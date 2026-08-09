/**
 * Reels Downloader API - powered by yt-dlp (real extraction, no fake data).
 *
 * Requires the yt-dlp binary to be installed and available on PATH in the
 * deployment environment (see Dockerfile). Supports Instagram, TikTok,
 * Facebook, YouTube Shorts, X/Twitter, Pinterest, and anything else yt-dlp
 * supports.
 */

const express = require('express');
const cors = require('cors');
const { execFile, spawn } = require('child_process');
const util = require('util');

const execFileAsync = util.promisify(execFile);

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

function detectPlatform(url) {
  const lower = url.toLowerCase();
  if (lower.includes('instagram.com') || lower.includes('instagr.am')) return 'Instagram';
  if (lower.includes('tiktok.com')) return 'TikTok';
  if (lower.includes('facebook.com') || lower.includes('fb.watch')) return 'Facebook';
  if (lower.includes('youtube.com') || lower.includes('youtu.be')) return 'YouTube';
  if (lower.includes('twitter.com') || lower.includes('x.com')) return 'X';
  if (lower.includes('pinterest.com') || lower.includes('pin.it')) return 'Pinterest';
  return 'Social Media';
}

function formatDuration(totalSeconds) {
  if (!totalSeconds || totalSeconds <= 0) return '';
  const m = Math.floor(totalSeconds / 60);
  const s = Math.floor(totalSeconds % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
}

// Extracts real, direct download links using yt-dlp - no hardcoded fallback data.
app.post('/api/resolve', async (req, res) => {
  const { url } = req.body || {};

  if (!url || typeof url !== 'string' || !url.trim()) {
    return res.status(400).json({ success: false, error: 'رابط غير صالح' });
  }

  const cleanUrl = url.trim();

  try {
    const { stdout } = await execFileAsync(
      'yt-dlp',
      [
        '-j',
        '--no-warnings',
        '--no-playlist',
        '-f', 'best[acodec!=none][vcodec!=none]/best',
        '--socket-timeout', '20',
        cleanUrl
      ],
      { maxBuffer: 1024 * 1024 * 25, timeout: 35000 }
    );

    // yt-dlp -j can print one JSON object per line for playlists; we only asked
    // for a single item (--no-playlist), so take the first line.
    const firstLine = stdout.trim().split('\n')[0];
    const info = JSON.parse(firstLine);

    const platform = detectPlatform(cleanUrl);

    let streams = [];
    if (Array.isArray(info.formats) && info.formats.length > 0) {
      // Only offer formats that have BOTH video and audio muxed together -
      // never fall back to a video-only stream, otherwise playback has no sound.
      const combined = info.formats.filter(
        (f) => f.url && f.vcodec && f.vcodec !== 'none' && f.acodec && f.acodec !== 'none'
      );

      streams = combined
        .sort((a, b) => (b.height || 0) - (a.height || 0))
        .slice(0, 4)
        .map((f) => ({
          quality: f.height ? `${f.height}p` : (f.format_note || 'auto'),
          downloadUrl: f.url,
          estimatedSizeMb:
            f.filesize || f.filesize_approx
              ? +((f.filesize || f.filesize_approx) / (1024 * 1024)).toFixed(1)
              : null
        }));
    }

    // info.url is the direct result of our forced format selector above
    // (best[acodec!=none][vcodec!=none]/best), so it is guaranteed to have
    // audio whenever the platform has any muxed format available at all.
    if (streams.length === 0 && info.url) {
      streams = [{ quality: 'auto', downloadUrl: info.url, estimatedSizeMb: null }];
    }

    if (streams.length === 0) {
      return res.status(422).json({
        success: false,
        error: 'لم يتم العثور على وسائط قابلة للتحميل في هذا الرابط'
      });
    }

    return res.json({
      success: true,
      originalUrl: cleanUrl,
      platform,
      title: info.title || `${platform} Video`,
      author: info.uploader ? `@${info.uploader}` : `@${platform.toLowerCase()}_creator`,
      thumbnail: info.thumbnail || '',
      duration: formatDuration(info.duration),
      streams
    });
  } catch (err) {
    console.error('yt-dlp resolve error:', err.message);
    const message =
      err.killed || /timeout/i.test(err.message || '')
        ? 'انتهت مهلة الاستخراج، حاول مجدداً'
        : 'تعذر استخراج الفيديو من هذا الرابط، تأكد أنه رابط صالح وعام (غير خاص)';
    return res.status(422).json({ success: false, error: message });
  }
});

// Streams the ACTUAL video/audio bytes to the client by running yt-dlp
// end-to-end on the server and piping its output directly to the response.
// This is the key fix for CDN links that reject the phone's direct request
// (expired/signed URLs, missing cookies, anti-hotlink checks, etc.) - since
// yt-dlp itself does the real download here, every platform is handled the
// same reliable way, not just metadata extraction.
app.get('/api/download', (req, res) => {
  const { url, height, mode } = req.query;

  if (!url || typeof url !== 'string' || !url.trim()) {
    return res.status(400).json({ success: false, error: 'رابط غير صالح' });
  }

  const isAudio = mode === 'audio';
  const formatSelector = isAudio
    ? 'bestaudio/best'
    : height
      ? `best[height<=${height}][acodec!=none][vcodec!=none]/best[acodec!=none][vcodec!=none]/best`
      : 'best[acodec!=none][vcodec!=none]/best';

  const args = [];
  if (isAudio) {
    args.push('-x', '--audio-format', 'mp3');
  }
  args.push(
    '-f', formatSelector,
    '--no-warnings',
    '--no-playlist',
    '--socket-timeout', '20',
    '-o', '-',
    url.trim()
  );

  const ytdlp = spawn('yt-dlp', args, { stdio: ['ignore', 'pipe', 'pipe'] });

  let headersSent = false;
  let stderrBuf = '';

  ytdlp.stdout.once('data', () => {
    if (!headersSent) {
      headersSent = true;
      res.status(200);
      res.setHeader('Content-Type', isAudio ? 'audio/mpeg' : 'video/mp4');
      res.setHeader(
        'Content-Disposition',
        `attachment; filename="download.${isAudio ? 'mp3' : 'mp4'}"`
      );
    }
  });

  ytdlp.stdout.pipe(res);

  ytdlp.stderr.on('data', (chunk) => {
    stderrBuf += chunk.toString();
  });

  ytdlp.on('error', () => {
    if (!res.headersSent) {
      res.status(500).json({ success: false, error: 'فشل تشغيل أداة التحميل على الخادم' });
    }
  });

  ytdlp.on('close', (code) => {
    if (code !== 0 && !headersSent) {
      console.error('yt-dlp download error:', stderrBuf.slice(-2000));
      if (!res.headersSent) {
        res.status(422).json({
          success: false,
          error: 'تعذر تحميل الفيديو، الرابط قد يكون منتهي الصلاحية أو خاصاً'
        });
      }
    } else {
      res.end();
    }
  });

  req.on('close', () => {
    if (!ytdlp.killed) ytdlp.kill('SIGKILL');
  });
});

app.get('/health', (req, res) => res.json({ ok: true }));

app.listen(PORT, () => {
  console.log(`Reels Downloader API (yt-dlp powered) running on port ${PORT}`);
});

