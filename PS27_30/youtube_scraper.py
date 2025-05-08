import requests
from bs4 import BeautifulSoup
import json
import pandas as pd
import time
import random
import re
from datetime import datetime
from youtube_comment_downloader import *

class YouTubeScraper:
    def __init__(self):
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept-Language': 'en-US,en;q=0.9'
        }
        self.downloader = YoutubeCommentDownloader()

    def extract_video_id(self, url):
        """Extract video ID from YouTube URL"""
        patterns = [
            r'(?:v=|\/)([0-9A-Za-z_-]{11}).*',
            r'(?:embed\/)([0-9A-Za-z_-]{11})',
            r'(?:youtu\.be\/)([0-9A-Za-z_-]{11})'
        ]
        
        for pattern in patterns:
            match = re.search(pattern, url)
            if match:
                return match.group(1)
        raise ValueError("Invalid YouTube URL")

    def get_video_info(self, video_id):
        """Get video information using YouTube's API"""
        url = f"https://www.youtube.com/watch?v={video_id}"
        response = requests.get(url, headers=self.headers)
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Extract data from meta tags
        video_info = {}
        video_info['title'] = soup.find('meta', property='og:title')['content']
        video_info['description'] = soup.find('meta', property='og:description')['content']
        video_info['thumbnail'] = soup.find('meta', property='og:image')['content']
        
        # Extract JSON data from page with updated structure
        for script in soup.find_all('script'):
            if 'ytInitialData' in str(script):
                try:
                    data_str = str(script).split('var ytInitialData = ')[1].split(';</script>')[0]
                    data = json.loads(data_str)
                    
                    # Navigate through the new JSON structure
                    video_data = data['contents']['twoColumnWatchNextResults']['results']['results']['contents'][0]['videoPrimaryInfoRenderer']
                    
                    # Extract view count
                    view_count = video_data['viewCount']['videoViewCountRenderer']['viewCount']['simpleText']
                    video_info['views'] = view_count
                    
                    # Extract like count
                    like_count = video_data['videoActions']['menuRenderer']['topLevelButtons'][0]['segmentedLikeDislikeButtonRenderer']['likeButton']['toggleButtonRenderer']['toggledText']['accessibility']['accessibilityData']['label']
                    video_info['likes'] = like_count
                    
                    # Extract channel name
                    channel = data['contents']['twoColumnWatchNextResults']['results']['results']['contents'][1]['videoSecondaryInfoRenderer']['owner']['videoOwnerRenderer']['title']['runs'][0]['text']
                    video_info['channel'] = channel
                    
                    break
                except Exception as e:
                    print(f"Error parsing JSON data: {e}")
                    continue
                
        return video_info

    def get_comments(self, video_id, max_comments=50):
        """Get video comments using youtube-comment-downloader"""
        comments = []
        url = f'https://youtube.com/watch?v={video_id}'
        
        try:
            # Get comments using the downloader
            for comment in self.downloader.get_comments_from_url(url, sort_by=SORT_BY_RECENT):
                if len(comments) >= max_comments:
                    break
                    
                comments.append({
                    'author': comment['author'],
                    'text': comment['text'],
                    'likes': comment.get('votes', 0),
                    'published': comment['time']
                })
                
                # Add a small delay between comment fetches
                time.sleep(random.uniform(0.1, 0.3))
            
        except Exception as e:
            print(f"Error fetching comments: {e}")
            
        return comments

    def save_to_csv(self, video_info, comments, video_id):
        """Save scraped data to CSV files"""
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        
        # Clean the title to make it filesystem-friendly
        clean_title = re.sub(r'[^\w\s-]', '', video_info['title'])
        clean_title = clean_title.strip().replace(' ', '_')[:50]  # Limit length to 50 chars
        
        filename = f'{clean_title}_{timestamp}.csv'
        
        # Create DataFrames
        video_df = pd.DataFrame([video_info])
        comments_df = pd.DataFrame(comments)
        
        # Add section headers
        with open(filename, 'w', newline='', encoding='utf-8') as f:
            f.write("VIDEO INFORMATION\n")
        
        # Save video info
        video_df.to_csv(filename, mode='a', index=False)
        
        # Add blank line and comments header
        with open(filename, 'a', newline='', encoding='utf-8') as f:
            f.write("\nCOMMENTS\n")
        
        # Save comments
        comments_df.to_csv(filename, mode='a', index=False)
        
        print(f"Data saved to {filename}")

def main():
    scraper = YouTubeScraper()
    
    try:
        # Get video URL from user
        video_url = input("Enter YouTube video URL: ")
        max_comments = int(input("Enter maximum number of comments to scrape (default 50): ") or 50)
        
        # Extract video ID
        video_id = scraper.extract_video_id(video_url)
        print(f"\nExtracting data for video ID: {video_id}")
        
        # Get video information
        print("Fetching video information...")
        video_info = scraper.get_video_info(video_id)
        
        # Get comments
        print(f"Fetching up to {max_comments} comments...")
        comments = scraper.get_comments(video_id, max_comments)
        
        # Save data
        print("\nSaving data to CSV files...")
        scraper.save_to_csv(video_info, comments, video_id)
        
        print(f"\nSuccessfully scraped:")
        print(f"- Video information")
        print(f"- {len(comments)} comments")
        print("\nData has been saved to CSV files in the current directory.")
        
    except Exception as e:
        print(f"\nAn error occurred: {e}")
        print("\nTroubleshooting tips:")
        print("1. Check if the YouTube URL is valid")
        print("2. Verify your internet connection")
        print("3. Try again in a few minutes")

if __name__ == "__main__":
    main()