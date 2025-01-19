# Microservice 1: Fetch Company Data (Filter1)
from flask import Flask, jsonify
import requests
from bs4 import BeautifulSoup
import pandas as pd

app = Flask(__name__)

@app.route('/fetch-companies', methods=['GET'])
def fetch_companies():
    MARKET_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb"

    try:
        response = requests.get(MARKET_DATA_URL)
        response.raise_for_status()
        page_content = response.text

        soup = BeautifulSoup(page_content, 'html.parser')
        dropdown_menu = soup.select_one("select#Code")

        company_codes = []
        if dropdown_menu:
            dropdown_options = dropdown_menu.find_all("option")
            for option in dropdown_options:
                company_code = option.get("value")
                if company_code and company_code.strip().isalpha():
                    company_codes.append(company_code)

        # Return the list of company codes as a JSON response
        return jsonify({"company_codes": company_codes})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5001)