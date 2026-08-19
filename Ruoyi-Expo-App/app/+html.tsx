import { ScrollViewStyleReset } from 'expo-router/html';

export default function Root({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <head>
        <meta charSet="utf-8" />
        <meta httpEquiv="X-UA-Compatible" content="IE=edge" />
        <meta
          name="viewport"
          content="width=device-width, initial-scale=1, viewport-fit=cover, shrink-to-fit=no"
        />
        <ScrollViewStyleReset />
        <style dangerouslySetInnerHTML={{ __html: responsiveBackground }} />
      </head>
      <body>{children}</body>
    </html>
  );
}

const responsiveBackground = `
html, body, #root {
  height: 100%;
}
body {
  background-color: #050B1C;
}
@media (min-width: 481px) {
  #root {
    max-width: 480px;
    margin: 0 auto;
    background-color: #050B1C;
    min-height: 100%;
    box-shadow: 0 0 24px rgba(0, 0, 0, 0.06);
  }
}
@media (prefers-color-scheme: dark) {
  body {
    background-color: #050B1C;
  }
  @media (min-width: 481px) {
    #root {
      background-color: #050B1C;
    }
  }
}
`;
