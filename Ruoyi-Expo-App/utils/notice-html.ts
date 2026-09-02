const SCRIPT_RE = /<script[\s\S]*?>[\s\S]*?<\/script>/gi;
const EVENT_ATTR_RE = /\son[a-z]+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi;

export function looksLikeHtml(value: string): boolean {
  return /<\/?[a-z][\s\S]*>/i.test(value);
}

/** 去掉脚本，保留对齐、缩进、加粗等排版标签 */
export function sanitizeNoticeHtml(html: string): string {
  return html
    .replace(SCRIPT_RE, '')
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, '')
    .replace(EVENT_ATTR_RE, '')
    .trim();
}

/** 后台富文本是 Quill，对齐写在 class 上，不是 style */
export function noticeHtmlCss(scope = ''): string {
  const s = scope ? `${scope} ` : '';
  return `
  ${s}p{margin:0;}
  ${s}strong,b{font-weight:700;}
  ${s}em,i{font-style:italic;}
  ${s}u{text-decoration:underline;}
  ${s}img{max-width:100%;height:auto;display:block;margin:8px 0;}
  ${s}table{width:100%;border-collapse:collapse;}
  ${s}.ql-align-left{text-align:left;}
  ${s}.ql-align-center{text-align:center;}
  ${s}.ql-align-right{text-align:right;}
  ${s}.ql-align-justify{text-align:justify;}
  ${s}.ql-indent-1{padding-left:2em;}
  ${s}.ql-indent-2{padding-left:4em;}
  ${s}.ql-indent-3{padding-left:6em;}
  ${s}.ql-indent-4{padding-left:8em;}
  ${s}.ql-size-small{font-size:0.75em;}
  ${s}.ql-size-large{font-size:1.5em;}
  ${s}.ql-size-huge{font-size:2.5em;}
`;
}

export function wrapNoticeHtmlDocument(html: string): string {
  const body = sanitizeNoticeHtml(html) || '暂无内容';
  return `<!DOCTYPE html><html><head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
<style>
  html,body{margin:0;padding:0;background:transparent;color:rgba(220,232,255,0.92);font:14px/1.75 -apple-system,BlinkMacSystemFont,'PingFang SC','Noto Sans SC',sans-serif;word-wrap:break-word;}
  body *{color:inherit !important;}
  ${noticeHtmlCss()}
</style>
</head><body>${body}</body></html>`;
}
