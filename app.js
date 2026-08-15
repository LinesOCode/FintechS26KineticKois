// InvestEasy - app.js
// Minimal interactive charts and CSV upload for teaching investing

document.addEventListener('DOMContentLoaded',()=>{
  initTabs();
  const samples = makeSampleDatasets();
  initMarket(samples);
  initPortfolio();
  setupFileUpload();
});

function initTabs(){
  const buttons=document.querySelectorAll('.tabs button');
  buttons.forEach(b=>b.addEventListener('click',()=>{
    buttons.forEach(x=>x.classList.remove('active'));
    b.classList.add('active');
    const tab=b.dataset.tab;
    document.querySelectorAll('.tab-content').forEach(s=>s.classList.toggle('active', s.id===tab));
  }));
}

function makeSampleDatasets(){
  // generate simple random-walk series for teaching
  const names=['AAPL','MSFT','SPY','BND','GLD'];
  const datasets={};
  names.forEach(name=>{datasets[name]=makeSeries(720, {drift: name==='BND'?0.0001: name==='GLD'?0.0002:0.0006, vol: name==='BND'?0.003:0.015});});
  return datasets;
}

function makeSeries(days, opts){
  const drift=opts.drift||0.0005; const vol=opts.vol||0.02;
  const vals=[]; let price=100+Math.random()*40;
  const labels=[]; const now=new Date();
  for(let i=0;i<days;i++){ const ret = drift + randn()*vol; price = Math.max(2, price*(1+ret)); vals.push(Number(price.toFixed(2))); const d=new Date(now); d.setDate(now.getDate()- (days-i)); labels.push(d.toISOString().slice(0,10)); }
  return {labels,vals};
}

function randn(){ // approx normal
  let u=0,v=0; while(u===0) u=Math.random(); while(v===0) v=Math.random(); return Math.sqrt(-2*Math.log(u))*Math.cos(2*Math.PI*v);
}

// Market chart
let marketChart;
function initMarket(samples){
  const datasetSelect=document.getElementById('datasetSelect');
  Object.keys(samples).forEach(k=>{const o=document.createElement('option');o.value=k;o.textContent=k;datasetSelect.appendChild(o)});
  document.getElementById('daysRange').addEventListener('input',e=>{document.getElementById('daysVal').textContent=e.target.value; updateMarketChart(samples, datasetSelect.value, +e.target.value);});
  datasetSelect.addEventListener('change',()=>updateMarketChart(samples,datasetSelect.value,+document.getElementById('daysRange').value));
  // create canvas
  const ctx=document.getElementById('marketChart').getContext('2d');
  marketChart=new Chart(ctx,{type:'line',data:{labels:[],datasets:[{label:'Price',data:[],borderColor:'#6ee7b7',pointRadius:0,fill:false}]},options:{responsive:true,plugins:{legend:{display:false}}}});
  // init
  datasetSelect.value='AAPL'; updateMarketChart(samples,'AAPL',+document.getElementById('daysRange').value);
}

function updateMarketChart(samples, name, days){
  const data=samples[name];
  const start = Math.max(0, data.labels.length-days);
  marketChart.data.labels = data.labels.slice(start);
  marketChart.data.datasets[0].data = data.vals.slice(start);
  marketChart.data.datasets[0].label = name;
  marketChart.update();
}

// Portfolio simulator
let portfolioChart;
function initPortfolio(){
  const stocksRange=document.getElementById('stocksRange');
  const bondsRange=document.getElementById('bondsRange');
  const cashRange=document.getElementById('cashRange');
  const stocksVal=document.getElementById('stocksVal');
  const bondsVal=document.getElementById('bondsVal');
  const cashVal=document.getElementById('cashVal');
  [stocksRange,bondsRange,cashRange].forEach(r=>r.addEventListener('input',()=>{
    const s=+stocksRange.value, b=+bondsRange.value, c=+cashRange.value;
    const total = s+b+c || 1;
    stocksVal.textContent=Math.round(s/total*100)+'%';
    bondsVal.textContent=Math.round(b/total*100)+'%';
    cashVal.textContent=Math.round(c/total*100)+'%';
    updatePortfolioChart({stocks:s/total,bonds:b/total,cash:c/total});
  }));
  // create chart
  const ctx=document.getElementById('portfolioChart').getContext('2d');
  portfolioChart=new Chart(ctx,{type:'line',data:{labels:[],datasets:[{label:'Portfolio Value',data:[],borderColor:'#60a5fa',pointRadius:0,fill:false}]},options:{responsive:true,plugins:{legend:{display:false}}}});
  // initial
  updatePortfolioChart({stocks:0.6,bonds:0.3,c:0.1});
}

function updatePortfolioChart(weights){
  // simulate 365 days using simple returns for assets
  const days=365; const labels=[]; const vals=[]; let portfolio=10000;
  const annual = {stocks:0.08,bonds:0.03,cash:0.005}; // simple expected returns
  const daily = {stocks:annual.stocks/252, bonds:annual.bonds/252, cash:annual.cash/252};
  for(let i=0;i<days;i++){ labels.push(new Date(Date.now()- (days-i)*24*3600*1000).toISOString().slice(0,10));
    const r = (daily.stocks * weights.stocks) + (daily.bonds * weights.bonds) + (daily.cash * weights.c);
    // add noise
    const noise = randn()* (0.01 * (weights.stocks));
    portfolio = portfolio * (1 + r + noise);
    vals.push(Number(portfolio.toFixed(2)));
  }
  portfolioChart.data.labels=labels; portfolioChart.data.datasets[0].data=vals; portfolioChart.update();
}

// File upload
function setupFileUpload(){
  const input=document.getElementById('fileInput');
  const status=document.getElementById('uploadStatus');
  input.addEventListener('change',e=>{
    const f=e.target.files[0]; if(!f) return; status.textContent='Processing '+f.name;
    const reader=new FileReader();
    reader.onload = ev=>{
      const text=ev.target.result;
      const parsed=parseCSV(text);
      if(parsed.length<2){ status.textContent='CSV parse failed or too few rows'; return; }
      // assume Date,Price header optional
      const labels=[]; const data=[];
      parsed.forEach(row=>{ const d=row[0]; const p=parseFloat(row[1]); if(!isNaN(p)) { labels.push(d); data.push(p); } });
      if(data.length===0){ status.textContent='No numeric price column found'; return; }
      // add to market chart as extra dataset
      marketChart.data.datasets.push({label:f.name,data:data,borderColor:randomColor(),pointRadius:0,fill:false});
      marketChart.data.labels = labels; marketChart.update(); status.textContent='Plotted '+f.name;
    };
    reader.readAsText(f);
  });
}

function parseCSV(text){
  const lines=text.split(/\r?\n/).map(l=>l.trim()).filter(Boolean);
  return lines.map(l=>l.split(/,|;|\t/).map(c=>c.trim()));
}

function randomColor(){
  const h=Math.floor(Math.random()*360); return `hsl(${h} 80% 65%)`;
}
