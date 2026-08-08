# SaveFlow Backend (yt-dlp powered)

Real video extraction API used by the SaveFlow Android app. Replaces the old
fake/hardcoded sample-video responses with real `yt-dlp` extraction — no
Instagram/TikTok scraping tricks, no third-party bot-protected APIs.

## What it does

`POST /api/resolve` with `{ "url": "<link>" }` runs `yt-dlp -j <url>` on the
server, parses the real metadata (title, author, thumbnail, duration) and
real direct download URLs, and returns them as JSON. If yt-dlp can't extract
anything (private post, deleted, unsupported site), it returns a proper error
— never fake sample data.

## Requirements

- Node.js 20+
- Python 3 + `yt-dlp` installed and available on `PATH`
- `ffmpeg` (needed by yt-dlp for some merges)

The included `Dockerfile` sets all of this up automatically — you don't need
to install anything manually if you deploy with Docker.

## Deploying (Railway example)

1. Push this `backend/` folder to your repo (already done if you're reading
   this from the repo).
2. On [Railway](https://railway.app): New Project → Deploy from GitHub repo →
   select this repo → set **Root Directory** to `backend`.
3. Railway will detect the `Dockerfile` and build it automatically.
4. Once deployed, Railway gives you a public URL like
   `https://your-app.up.railway.app`.
5. Copy that URL — you'll need it in the Android app (see below).

Render.com works the same way (New → Web Service → point Root Directory to
`backend`, it will also pick up the Dockerfile).

## Connecting the Android app

Open `app/src/main/java/com/example/data/service/Constants.kt` and set:

```kotlin
const val BACKEND_BASE_URL = "https://your-app.up.railway.app"
```

(no trailing slash). Rebuild the app.

## Local testing

```bash
cd backend
npm install
# make sure yt-dlp is installed locally: pip install -U yt-dlp
node server.js
```

Then test with:
```bash
curl -X POST http://localhost:3000/api/resolve \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.instagram.com/reel/XXXXXXXXXXX/"}'
```

## Notes

- Only works for **public** posts/videos (private content requires a logged
  -in session, which this server does not have).
- yt-dlp is actively maintained and gets patched quickly when platforms
  change their sites — keep the Docker image's yt-dlp updated periodically
  by rebuilding (the Dockerfile always pulls the latest version at build
  time).
