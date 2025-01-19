# Microservice 2: Process Historical Data (Filter2)
from flask import Flask, request, jsonify
from datetime import datetime
import pandas as pd
import requests
from bs4 import BeautifulSoup
import os

app = Flask(__name__)

@app.route('/process-historical-data', methods=['POST'])
def process_historical_data():
    data = request.get_json()
    if not data or 'company_code' not in data:
        return jsonify({"error": "Missing 'company_code' in request data."}), 400

    company_code = data['company_code']
    HISTORICAL_DATA_URL = f"https://www.mse.mk/mk/stats/symbolhistory/{company_code}"

    try:
        current_year = datetime.now().year
        all_data = []

        for year in range(current_year, current_year - 10, -1):
            start_date = datetime(year, 1, 1)
            end_date = datetime(year, 12, 31)
            payload = {
                "FromDate": start_date.strftime("%Y-%m-%d"),
                "ToDate": end_date.strftime("%Y-%m-%d"),
            }

            response = requests.post(HISTORICAL_DATA_URL, data=payload)
            response.raise_for_status()
            document = BeautifulSoup(response.text, 'html.parser')
            results_table = document.select_one("table#resultsTable")

            if results_table:
                rows = results_table.select("tbody tr")
                for row in rows:
                    columns = row.select("td")
                    if columns:
                        all_data.append({
                            "record_date": columns[0].text,
                            "last_price": columns[1].text,
                            "high_price": columns[2].text,
                            "low_price": columns[3].text,
                            "avg_price": columns[4].text,
                            "percent_change": columns[5].text,
                            "volume": columns[6].text,
                        })

        # Return the historical data as a JSON response
        return jsonify({"historical_data": all_data})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5002)
