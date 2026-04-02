import ReactMarkdown from 'react-markdown';
import { useState } from 'react';

function CopyButton({ code }) {
  const [copied, setCopied] = useState(false);
  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  return (
    <button className="copy-btn" onClick={handleCopy}>
      {copied ? '✅ Copied!' : '📋 Copy'}
    </button>
  );
}

function CodeBlock({ className, children }) {
  const code = String(children).trim();
  const language = className?.replace('language-', '') || 'code';
  return (
    <div className="code-block-wrapper">
      <div className="code-block-header">
        <span className="code-lang">{language}</span>
        <CopyButton code={code} />
      </div>
      <pre><code>{code}</code></pre>
    </div>
  );
}

const markdownComponents = {
  code({ node, inline, className, children, ...props }) {
    const code = String(children).trim();
    
    // ✅ Treat as inline if: inline prop is true OR no newlines in content
    const isInline = inline || !code.includes('\n');
    
    if (isInline) {
      return (
        <code style={{
          background: 'rgba(124, 58, 237, 0.15)',
          border: '1px solid rgba(124, 58, 237, 0.25)',
          borderRadius: '4px',
          padding: '0.1rem 0.4rem',
          fontFamily: "'Fira Code', 'Courier New', monospace",
          fontSize: '0.82rem',
          color: '#c4b5fd',
        }}>
          {code}
        </code>
      );
    }
    
    return <CodeBlock className={className}>{children}</CodeBlock>;
  }
};

// ✅ Split markdown into sections by headings (##, ###, **Title**, numbered)
function splitIntoSections(content) {
  const lines = content.split('\n');
  const sections = [];
  let current = [];

  for (const line of lines) {
    // ✅ Only split on actual headings (# ## ###), NOT numbered lists
    const isHeading = /^#{1,3}\s/.test(line);

    if (isHeading && current.length > 0) {
      const text = current.join('\n').trim();
      if (text) sections.push(text);
      current = [line];
    } else {
      current.push(line);
    }
  }

  if (current.length > 0) {
    const text = current.join('\n').trim();
    if (text) sections.push(text);
  }

  return sections.length > 1 ? sections : null;
} 

export default function MessageBubble({ role, content }) {
  const isUser = role === 'user';

  if (isUser) {
    return (
      <div className="message-row user-row">
        <div className="message-bubble user-bubble">{content}</div>
      </div>
    );
  }

  const sections = splitIntoSections(content);

  return (
    <div className="message-row assistant-row">
      <div className="assistant-icon">🤖</div>
      <div className="assistant-cards-wrapper">
        {sections ? (
          sections.map((section, i) => (
            <div key={i} className="assistant-card">
              <ReactMarkdown components={markdownComponents}>{section}</ReactMarkdown>
            </div>
          ))
        ) : (
          <div className="assistant-card">
            <ReactMarkdown components={markdownComponents}>{content}</ReactMarkdown>
          </div>
        )}
      </div>
    </div>
  );
}