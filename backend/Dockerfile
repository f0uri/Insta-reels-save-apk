# Backend for SaveFlow reels downloader - runs Node.js + real yt-dlp
FROM node:20-slim

# Install Python + yt-dlp + ffmpeg (ffmpeg needed for merging video/audio streams)
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip ffmpeg ca-certificates curl && \
    pip3 install --no-cache-dir --break-system-packages -U yt-dlp && \
    apt-get purge -y --auto-remove python3-pip curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY package*.json ./
RUN npm install --omit=dev

COPY . .

ENV PORT=3000
EXPOSE 3000

CMD ["node", "server.js"]
