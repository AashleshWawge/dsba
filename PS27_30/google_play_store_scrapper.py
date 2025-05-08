from google_play_scraper import reviews, Sort
import pandas as pd
from urllib.parse import urlparse, parse_qs

def extract_package_name(url):
    parsed_url = urlparse(url)
    query_params = parse_qs(parsed_url.query)
    return query_params.get('id', [None])[0]

def main():
    # Step 1: Get app URL
    app_url = input("Enter the Google Play Store app URL: ").strip()

    # Step 2: Extract package name
    package_name = extract_package_name(app_url)
    if not package_name:
        print("❌ Invalid URL. Please ensure it includes '?id=' parameter.")
        return

    print(f"\n🔍 Fetching reviews for: {package_name}")

    try:
        # Step 3: Fetch reviews
        result, _ = reviews(
            package_name,
            lang='en',
            country='us',
            sort=Sort.NEWEST,
            count=100,
            filter_score_with=None
        )

        # Step 4: Convert to DataFrame
        df = pd.DataFrame(result)
        df = df[['userName', 'score', 'at', 'content']]  # Keep useful columns

        # Step 5: Save to CSV
        filename = f"{package_name}_reviews.csv"
        df.to_csv(filename, index=False, encoding='utf-8-sig')

        print(f"\n✅ Reviews saved to '{filename}'")

    except Exception as e:
        print(f"⚠️ Error occurred: {e}")

if __name__ == "__main__":
    main()
