import glob       #to get filenames
import datetime   #to check in weekly folder paths
import pyimgur    #to consolidate images to albums in imgur
import praw       #to post links to reddit/r/slashw
import OAuth2Util #OAuth2 for reddit
import os         #to check for empty files
import requests   #to see how many imgur requests remain

def assign_directory_by_time():
    path = 'Y'
    date = datetime.datetime.now()
    path = path + str(date.year)
    path = path + '-W' + str(date.isocalendar()[1] - 1)  #the week of the year
    return path

PATH_BASE = '/media/UNTITLED/Wallpapers/' + assign_directory_by_time()

config = open('/home/pi/GitHub/wallpaperBot/config.txt', 'r')

CLIENT_ID = config.readline()
CLIENT_SECRET = config.readline()
config.close()
imgur = pyimgur.Imgur(CLIENT_ID, CLIENT_SECRET)


USER_AGENT = '4chan /w/ crossposter for /u/Shazambom'
SUBREDDIT = 'slashw'

PATH_BASE = '/media/UNTITLED/Wallpapers/' + assign_directory_by_time()

def consolidate_to_albums():
    filenames = glob.glob(PATH_BASE + '/*.txt')
    
    r = praw.Reddit(user_agent=USER_AGENT)
    o = OAuth2Util.OAuth2Util(r)
    print('I am authorized by reddit')
    o.refresh(force=True)
    
    for filename in filenames:
        images = []
        if is_empty(filename):
           os.remove(filename)
           continue
        
        file = open(filename, 'r')
        
        for line in file:
            images.append(imgur.get_at_url(line))

        title = filename.rsplit('/', 1)[1][:-4]
        album = imgur.create_album(title=title, images=images)
        print('album made at ' + album.link)

        imgur_requests = requests.get("https://api.imgur.com/3/credits")
        print (imgur_requests.content)
        
        r.submit(SUBREDDIT, album.title, url=album.link)
        print ('post submitted')
        
        file.close()
        os.remove(filename)    
        
def is_empty(filename):
    return os.stat(filename).st_size==0

print(assign_directory_by_time())
consolidate_to_albums()

