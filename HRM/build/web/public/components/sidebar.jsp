<%@ page pageEncoding="UTF-8" %>

<style>
  .left-panel {
    flex: 0 0 58%;
    position: relative;
    overflow: hidden;
    background: #0b0e2a;
  }
  .left-panel::before {
    content: '';
    position: absolute; inset: 0;
    background:
      radial-gradient(ellipse 80% 60% at 20% 110%, #1a2a6c 0%, transparent 60%),
      radial-gradient(ellipse 60% 50% at 80% -10%, #2d1b69 0%, transparent 55%),
      radial-gradient(ellipse 40% 40% at 50% 50%, #0f1535 0%, transparent 80%),
      linear-gradient(160deg, #08091f 0%, #0d1240 40%, #0a0e2e 100%);
    z-index: 0;
  }
  .stars {
    position: absolute; inset: 0; z-index: 1;
    overflow: hidden;
  }
  .star {
    position: absolute;
    background: #fff;
    border-radius: 50%;
    animation: twinkle var(--d) ease-in-out infinite alternate;
  }
  @keyframes twinkle {
    from { opacity: var(--min); }
    to   { opacity: var(--max); }
  }
  .planet {
    position: absolute;
    border-radius: 50%;
    z-index: 2;
  }
  .planet-1 {
    width: 90px; height: 90px;
    background: radial-gradient(circle at 35% 35%, #7b5ea7, #3d2b6b);
    top: 6%; right: 18%;
    box-shadow: inset -8px -8px 20px rgba(0,0,0,.5);
    animation: floatP 7s ease-in-out infinite;
  }
  .planet-1::after {
    content: '';
    position: absolute;
    top: 50%; left: -30%;
    width: 160%; height: 28%;
    border: 3px solid rgba(180,140,220,.35);
    border-radius: 50%;
    transform: translateY(-50%) rotateX(70deg);
  }
  .planet-2 {
    width: 38px; height: 38px;
    background: radial-gradient(circle at 35% 35%, #4fc3f7, #1565c0);
    top: 12%; right: 8%;
    box-shadow: inset -4px -4px 10px rgba(0,0,0,.4);
    animation: floatP 5s ease-in-out infinite reverse;
  }
  .planet-3 {
    width: 22px; height: 22px;
    background: radial-gradient(circle at 35% 35%, #a5d6a7, #2e7d32);
    top: 28%; right: 6%;
    animation: floatP 6s ease-in-out 1s infinite;
  }
  @keyframes floatP {
    0%,100% { transform: translateY(0); }
    50% { transform: translateY(-14px); }
  }
  .tagline {
    position: absolute;
    top: 50%; left: 7%;
    transform: translateY(-58%);
    z-index: 10;
  }
  .tagline h1 {
    font-size: clamp(2.4rem, 4vw, 3.8rem);
    font-weight: 800;
    color: #fff;
    line-height: 1.12;
    letter-spacing: -.5px;
  }
  .illustration {
    position: absolute;
    bottom: 10%; left: 50%;
    transform: translateX(-45%);
    z-index: 10;
    width: 420px;
  }
  .bottom-bar {
    position: absolute;
    bottom: 1.5rem; left: 0; right: 0;
    display: flex;
    justify-content: center;
    z-index: 20;
  }
  .bottom-bar a {
    color: rgba(255,255,255,.7);
    font-size: .78rem;
    text-decoration: none;
    background: rgba(255,255,255,.08);
    border: 1px solid rgba(255,255,255,.15);
    padding: .45rem 1.1rem;
    border-radius: 20px;
    display: flex; align-items: center; gap: .5rem;
    transition: background .2s;
  }
  .bottom-bar a:hover { background: rgba(255,255,255,.15); }
</style>

<div class="left-panel">
  <div class="stars" id="stars"></div>

  <div class="planet planet-1"></div>
  <div class="planet planet-2"></div>
  <div class="planet planet-3"></div>

  <div class="tagline">
    <h1>Make<br>Business<br>Better</h1>
  </div>

  <div class="illustration">
    <svg viewBox="0 0 500 340" fill="none" xmlns="http://www.w3.org/2000/svg">
      <ellipse cx="250" cy="290" rx="200" ry="30" fill="rgba(30,50,150,.3)"/>
      <path d="M100 220 L250 160 L400 220 L250 280 Z" fill="#1a3a8f"/>
      <path d="M100 220 L100 240 L250 300 L250 280 Z" fill="#0d2060"/>
      <path d="M400 220 L400 240 L250 300 L250 280 Z" fill="#1530a0"/>
      <path d="M140 230 L250 175 L360 230" stroke="rgba(100,180,255,.4)" stroke-width="1.5" fill="none"/>
      <path d="M160 240 L250 190 L340 240" stroke="rgba(100,180,255,.25)" stroke-width="1" fill="none"/>
      <rect x="165" y="200" width="24" height="24" rx="2" fill="rgba(50,120,255,.5)" transform="rotate(-30 177 212)"/>
      <rect x="305" y="200" width="24" height="24" rx="2" fill="rgba(50,120,255,.5)" transform="rotate(30 317 212)"/>
      <rect x="235" y="185" width="18" height="18" rx="2" fill="rgba(80,160,255,.6)" transform="rotate(-10 244 194)"/>
      <rect x="195" y="100" width="110" height="80" rx="8" fill="#1e3c9a" stroke="rgba(100,180,255,.6)" stroke-width="2"/>
      <rect x="200" y="105" width="100" height="70" rx="6" fill="#0d2270"/>
      <circle cx="250" cy="140" r="22" fill="none" stroke="rgba(80,200,255,.7)" stroke-width="3"/>
      <circle cx="250" cy="140" r="14" fill="none" stroke="rgba(80,200,255,.4)" stroke-width="2"/>
      <circle cx="250" cy="140" r="6" fill="rgba(80,200,255,.8)"/>
      <rect x="240" y="180" width="20" height="12" rx="2" fill="#1e3c9a"/>
      <rect x="230" y="192" width="40" height="5" rx="2" fill="#1a3082"/>
      <ellipse cx="210" cy="195" rx="22" ry="25" fill="#e8923a"/>
      <circle cx="210" cy="162" r="18" fill="#f5cba7"/>
      <path d="M192 158 Q210 145 228 158 Q225 150 210 147 Q195 150 192 158Z" fill="#6b3a2a"/>
      <path d="M232 182 Q255 165 270 158" stroke="#e8923a" stroke-width="10" stroke-linecap="round" fill="none"/>
      <circle cx="272" cy="156" r="7" fill="#f5cba7"/>
      <path d="M198 218 Q190 235 185 250" stroke="#7b4fa0" stroke-width="12" stroke-linecap="round" fill="none"/>
      <path d="M222 218 Q228 235 230 250" stroke="#7b4fa0" stroke-width="12" stroke-linecap="round" fill="none"/>
      <ellipse cx="182" cy="253" rx="10" ry="6" fill="#4a3020"/>
      <ellipse cx="232" cy="253" rx="10" ry="6" fill="#4a3020"/>
      <g transform="translate(65,205)">
        <ellipse cx="0" cy="0" rx="26" ry="14" fill="#f5a623"/>
        <ellipse cx="0" cy="-6" rx="26" ry="14" fill="#f7c948"/>
        <text x="0" y="-2" text-anchor="middle" font-size="12" font-weight="bold" fill="#d4890a">$</text>
      </g>
      <g transform="translate(390,240)">
        <ellipse cx="0" cy="0" rx="30" ry="16" fill="#f5a623"/>
        <ellipse cx="0" cy="-7" rx="30" ry="16" fill="#f7c948"/>
        <text x="0" y="-2" text-anchor="middle" font-size="14" font-weight="bold" fill="#d4890a">$</text>
      </g>
      <g transform="translate(120,290)">
        <ellipse cx="0" cy="0" rx="20" ry="11" fill="#f5a623"/>
        <ellipse cx="0" cy="-5" rx="20" ry="11" fill="#f7c948"/>
        <text x="0" y="-1" text-anchor="middle" font-size="10" font-weight="bold" fill="#d4890a">$</text>
      </g>
      <path d="M100 270 Q120 255 140 270 Q160 285 180 265" stroke="rgba(100,180,255,.5)" stroke-width="2" fill="none" stroke-dasharray="4 3"/>
      <path d="M340 275 Q360 260 380 275" stroke="rgba(100,180,255,.5)" stroke-width="2" fill="none" stroke-dasharray="4 3"/>
      <path d="M310 230 L325 222 L340 230 L325 238 Z" fill="#3050d0"/>
      <path d="M310 230 L310 242 L325 250 L325 238 Z" fill="#1a3080"/>
      <path d="M340 230 L340 242 L325 250 L325 238 Z" fill="#2540b0"/>
      <path d="M150 245 L163 238 L176 245 L163 252 Z" fill="#3050d0"/>
      <path d="M150 245 L150 255 L163 262 L163 252 Z" fill="#1a3080"/>
      <path d="M176 245 L176 255 L163 262 L163 252 Z" fill="#2540b0"/>
      <circle cx="250" cy="318" r="12" fill="#f5cba7"/>
      <path d="M238 318 Q250 308 262 318" fill="#5a2d10"/>
      <circle cx="246" cy="316" r="2" fill="#5a2d10"/>
      <circle cx="254" cy="316" r="2" fill="#5a2d10"/>
      <ellipse cx="250" cy="140" rx="60" ry="60" fill="rgba(50,120,255,.06)"/>
      <ellipse cx="250" cy="220" rx="120" ry="50" fill="rgba(30,80,200,.08)"/>
    </svg>
  </div>

  <div class="bottom-bar">
    <a href="#">
      <i class="bi bi-box-arrow-up-right"></i>
       HRM - Phần mềm Quản trị nhân sự
    </a>
  </div>
</div>

<script>
  (function () {
    var starsEl = document.getElementById('stars');
    for (var i = 0; i < 120; i++) {
      var s = document.createElement('div');
      s.className = 'star';
      var size = Math.random() * 2.5 + .5;
      var minO = (Math.random() * .3 + .1).toFixed(2);
      var maxO = (Math.random() * .5 + .4).toFixed(2);
      s.style.cssText =
        'width:' + size + 'px;height:' + size + 'px;' +
        'top:' + (Math.random() * 100) + '%;' +
        'left:' + (Math.random() * 100) + '%;' +
        '--d:' + (Math.random() * 3 + 2).toFixed(1) + 's;' +
        '--min:' + minO + ';--max:' + maxO + ';' +
        'animation-delay:' + (Math.random() * 3).toFixed(1) + 's;';
      starsEl.appendChild(s);
    }
  }());
</script>
