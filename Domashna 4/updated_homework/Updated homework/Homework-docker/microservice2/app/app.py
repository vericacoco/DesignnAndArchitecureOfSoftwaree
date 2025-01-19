import csv
import os
import numpy as np
import pandas as pd
import requests
from bs4 import BeautifulSoup
from flask import Flask, render_template, request, jsonify
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
from textblob import TextBlob
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
                y.append(1 if data[i + lookback] > data[i + lookback - 1] else 0)
            return np.array(X), np.array(y)

        lookback = 30
        X, y = create_features_and_labels(df['last_price'].values, lookback)

        # Split into train and validation sets
        train_size = int(len(X) * 0.7)
        X_train, X_val = X[:train_size], X[train_size:]
        y_train, y_val = y[:train_size], y[train_size:]

        # Train RandomForest Classifier model
        model = RandomForestClassifier(n_estimators=100, random_state=42)
        model.fit(X_train, y_train)

        # Predict on validation set
        predictions = model.predict(X_val)

        # Evaluate the model
        accuracy = accuracy_score(y_val, predictions)
        print(f'Accuracy: {accuracy:.2f}')

        # Plot actual vs predicted prices
        plt.figure(figsize=(12, 6))
        plt.plot(y_val, color='blue', label='Actual Trend (Up=1, Down=0)')
        plt.plot(predictions, color='red', label='Predicted Trend')
        plt.title(f'{company_code} Stock Trend Prediction')
        plt.xlabel('Time')
        plt.ylabel('Trend (Up/Down)')
        plt.legend()
        plt.savefig('stock_trend_prediction.png')

        # Get latest prediction
        prediction_value = predictions[-1]

        return jsonify({"predicted_trend": "Up" if prediction_value == 1 else "Down"})

    except FileNotFoundError:
        return jsonify({"error": f"CSV file for {company_code} not found."}), 404

@app.route('/sentiment', methods=['POST'])
def sentiment_analysis():
    company_code = request.form.get('companyCode')
    news_url = f"https://finance.yahoo.com/quote/{company_code}/news"

    try:
        response = requests.get(news_url)
        soup = BeautifulSoup(response.text, 'html.parser')
        headlines = [item.get_text() for item in soup.find_all('h3')]

        sentiments = []
        for headline in headlines:
            analysis = TextBlob(headline)
            sentiment_score = analysis.sentiment.polarity
            sentiments.append({"headline": headline, "sentiment": sentiment_score})

        return jsonify({"company_code": company_code, "sentiments": sentiments})

    except Exception as e:
        return jsonify({"error": str(e)})

if __name__ == '__main__':
    app.run(debug=True, port=5002)
