export function parseM3U(content, sourceUrl = '') {
  const lines = content.split('\n');
  const channels = [];
  let currentEntry = null;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();

    if (line.startsWith('#EXTINF:')) {
      const match = line.match(/#EXTINF:(?:-1|0|\d+(?:\.\d+)?)(?:\s+(.*))?/);
      if (!match) continue;

      const rawAttrs = match[1] || '';
      const attrs = parseExtinfAttributes(rawAttrs);
      const name = extractDisplayName(rawAttrs);

      currentEntry = {
        name: name || attrs['tvg-name'] || 'Unknown Channel',
        logo: attrs['tvg-logo'] || '',
        group: attrs['group-title'] || 'Uncategorized',
        channelNumber: attrs['tvg-chno'] || '',
        tvgId: attrs['tvg-id'] || '',
        tvgName: attrs['tvg-name'] || '',
        url: '',
        source: sourceUrl,
      };
    } else if (line.startsWith('#EXTVLCOPT:')) {
      // vlc options, skip
    } else if (line.startsWith('#KODIPROP:')) {
      // kodi properties, skip
    } else if (line.startsWith('#EXTGRP:')) {
      if (currentEntry) {
        currentEntry.group = line.replace('#EXTGRP:', '').trim() || currentEntry.group;
      }
    } else if (line && !line.startsWith('#') && currentEntry) {
      currentEntry.url = line;
      channels.push({ ...currentEntry });
      currentEntry = null;
    } else if (line.startsWith('#EXTM3U')) {
      // header, ignore
    }
  }

  return channels;
}

function parseExtinfAttributes(raw) {
  const attrs = {};
  const regex = /(\w+(?:-\w+)*)\s*=\s*"([^"]*)"/g;
  let match;
  while ((match = regex.exec(raw)) !== null) {
    attrs[match[1]] = match[2];
  }
  return attrs;
}

function extractDisplayName(raw) {
  const lastQuote = raw.lastIndexOf('"');
  if (lastQuote !== -1 && lastQuote < raw.length - 1) {
    const afterQuote = raw.substring(lastQuote + 1).trim();
    if (afterQuote) return afterQuote;
  }
  const commaIndex = raw.indexOf(',');
  if (commaIndex !== -1) {
    return raw.substring(commaIndex + 1).trim();
  }
  return raw;
}

export function fetchM3U(url) {
  return fetch(url)
    .then(res => {
      if (!res.ok) throw new Error(`HTTP ${res.status}: Failed to fetch playlist`);
      return res.text();
    })
    .then(text => {
      const channels = parseM3U(text, url);
      return {
        url,
        name: url.split('/').pop()?.replace(/\.m3u8?$/i, '') || 'Playlist',
        channels,
        channelCount: channels.length,
        addedAt: new Date().toISOString(),
      };
    });
}
