import csv
import os
import numpy as np
import pandas as pd
from flask import Flask, render_template, request, jsonify
from sklearn.preprocessing import MinMaxScaler
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error
import matplotlib.pyplot as plt

app = Flask(__name__)

# Read company codes from issuers.csv
def read_company_codes():
    company_codes = []
    with open('issuers.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.reader(csvfile)
        next(reader)  # Skip header row
        for row in reader:
            company_codes.append(row[0])
    return company_codes

# Read historical stock data for a company
def read_historical_data(company_code):
    file_path = os.path.join(os.getcwd(), f'historical_data_{company_code}.csv')
    if os.path.exists(file_path):
        df = pd.read_csv(file_path)
        return df
    else:
        return None

@app.route('/')
def home():
    company_codes = read_company_codes()
    return render_template('index.html', company_codes=company_codes)

@app.route('/company', methods=['POST'])
def company_data():
    company_code = request.form.get('company')
    df = read_historical_data(company_code)
    if df is not None:
        historical_data = df.to_dict(orient="records")
        return render_template('company_data.html', company_code=company_code, historical_data=historical_data)
    else:
        return jsonify({"error": f"CSV file for {company_code} not found."}), 404

@app.route('/predict', methods=['POST'])
def predict():
    company_code = request.form.get('companyCode')

    csv_file = f"historical_data_{company_code}.csv"
    try:
        df = pd.read_csv(csv_file)

        df['record_date'] = pd.to_datetime(df['record_date'], format='%Y-%m-%d')
        df = df[['record_date', 'last_price']]
        df.set_index('record_date', inplace=True)

        # Data normalization
        scaler = MinMaxScaler(feature_range=(0, 1))
        df['last_price'] = scaler.fit_transform(df[['last_price']])

        # Prepare dataset for training
        def create_features_and_labels(data, lookback=30):
            X, y = [], []
            for i in range(len(data) - lookback):
                X.append(data[i:i + lookback])
                y.append(data[i + lookback])
            return np.array(X), np.array(y)

        lookback = 30
        X, y = create_features_and_labels(df['last_price'].values, lookback)

        # Split into train and validation sets
        train_size = int(len(X) * 0.7)
        X_train, X_val = X[:train_size], X[train_size:]
        y_train, y_val = y[:train_size], y[train_size:]

        # Train RandomForest model
        model = RandomForestRegressor(n_estimators=100, random_state=42)
        model.fit(X_train, y_train)

        # Predict on validation set
        predictions = model.predict(X_val)
        predictions = scaler.inverse_transform(predictions.reshape(-1, 1))
        y_val_actual = scaler.inverse_transform(y_val.reshape(-1, 1))

        # Calculate RMSE
        rmse = np.sqrt(mean_squared_error(y_val_actual, predictions))
        print(f'Root Mean Squared Error (RMSE): {rmse:.2f}')

        # Plot actual vs predicted prices
        plt.figure(figsize=(12, 6))
        plt.plot(y_val_actual, color='blue', label='Actual Stock Price')
        plt.plot(predictions, color='red', label='Predicted Stock Price')
        plt.title(f'{company_code} Stock Price Prediction')
        plt.xlabel('Time')
        plt.ylabel('Stock Price')
        plt.legend()
        plt.savefig('stock_prediction.png')

        # Get latest prediction
        prediction_value = predictions[-1][0]

        return jsonify({"predicted_price": float(prediction_value)})

    except FileNotFoundError:
        return jsonify({"error": f"CSV file for {company_code} not found."}), 404

if __name__ == '__main__':
    app.run(debug=True, port=5001)
