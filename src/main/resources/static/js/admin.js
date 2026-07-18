document.querySelectorAll('.tabs button').forEach(b=>b.addEventListener('click',()=>{document.querySelectorAll('.tabs button,.tab-pane').forEach(x=>x.classList.remove('active'));b.classList.add('active');document.getElementById(b.dataset.tab).classList.add('active')}));
function openModal(id){const m=document.getElementById(id);m.querySelector('form').reset();m.querySelector('[name=id]').value='';m.classList.add('open')}
function closeModal(el){el.closest('.modal').classList.remove('open')}
document.querySelectorAll('.modal').forEach(m=>m.addEventListener('click',e=>{if(e.target===m)m.classList.remove('open')}));
function fill(id,data,title){const m=document.getElementById(id);Object.entries(data).forEach(([k,v])=>{const x=m.querySelector(`[name=${k}]`);if(x)x.value=v??''});if(title)m.querySelector('h2').textContent=title;m.classList.add('open')}
function editCity(b){fill('cityModal',{id:b.dataset.id,name:b.dataset.name},'Chỉnh sửa thành phố')}
function editCinema(b){fill('cinemaModal',{id:b.dataset.id,name:b.dataset.name,address:b.dataset.address,cityId:b.dataset.city},'Chỉnh sửa rạp chiếu')}
function editRoom(b){fill('roomModal',{id:b.dataset.id,name:b.dataset.name,roomType:b.dataset.type,standardRows:b.dataset.standardRows,vipRows:b.dataset.vipRows,sweetboxRows:b.dataset.sweetboxRows,cinemaId:b.dataset.cinema},'Chỉnh sửa phòng chiếu');updateSeatPreview()}
function editCombo(b){fill('comboModal',{id:b.dataset.id,name:b.dataset.name,description:b.dataset.description,price:b.dataset.price,quantity:b.dataset.quantity,status:b.dataset.status},'Chỉnh sửa combo')}

/* Tổng số ghế = (hàng thường + hàng VIP) x 10 + hàng sweetbox x 8 (sweetbox là ghế đôi). */
function updateSeatPreview(){const f=document.querySelector('#roomModal form');if(!f)return;const n=x=>Math.max(0,parseInt(f[x].value,10)||0);const total=(n('standardRows')+n('vipRows'))*10+n('sweetboxRows')*8;const out=document.getElementById('seatPreview');if(out)out.textContent=total+' ghế';}
document.querySelectorAll('#roomModal input[type=number]').forEach(i=>i.addEventListener('input',updateSeatPreview));

/* ---- Modal xác nhận dùng chung: thay cho confirm() của trình duyệt ----
   Dùng: <form ... data-confirm="Nội dung" data-confirm-title="Tiêu đề"> */
document.addEventListener('submit',e=>{
  const form=e.target;const message=form.dataset.confirm;
  if(!message||form.dataset.confirmed==='1')return;
  e.preventDefault();
  const modal=document.getElementById('confirmModal');
  if(!modal){if(confirm(message))form.submit();return;}          // fallback nếu trang chưa nhúng modal
  document.getElementById('confirmTitle').textContent=form.dataset.confirmTitle||'Xác nhận';
  document.getElementById('confirmMessage').textContent=message;
  const ok=document.getElementById('confirmOk');
  const fresh=ok.cloneNode(true);ok.replaceWith(fresh);          // bỏ handler của lần mở trước
  fresh.textContent=form.dataset.confirmOk||'Xác nhận';
  fresh.addEventListener('click',()=>{modal.classList.remove('open');form.dataset.confirmed='1';form.submit()});
  modal.classList.add('open');
});
function range(days){const end=new Date(),start=new Date();start.setDate(end.getDate()-days+1);const f=d=>d.toISOString().slice(0,10);const form=document.querySelector('.date-filter');form.from.value=f(start);form.to.value=f(end);form.submit()}
(function draw(){const c=document.getElementById('revenueChart');if(!c||typeof chartValues==='undefined')return;const dpr=devicePixelRatio||1,w=c.clientWidth,h=260;c.width=w*dpr;c.height=h*dpr;const x=c.getContext('2d');x.scale(dpr,dpr);x.clearRect(0,0,w,h);const pad={l:55,r:15,t:20,b:35},iw=w-pad.l-pad.r,ih=h-pad.t-pad.b,max=Math.max(...chartValues.map(Number),1);x.strokeStyle='#2a2a30';x.fillStyle='#a0a0b0';x.font='10px Inter';for(let i=0;i<5;i++){const y=pad.t+ih*i/4;x.beginPath();x.moveTo(pad.l,y);x.lineTo(w-pad.r,y);x.stroke();x.fillText(Math.round(max*(4-i)/4/1000)+'k',8,y+3)}if(!chartValues.length)return;const pts=chartValues.map((v,i)=>[pad.l+(chartValues.length===1?iw/2:iw*i/(chartValues.length-1)),pad.t+ih-Number(v)/max*ih]);const g=x.createLinearGradient(0,pad.t,0,h-pad.b);g.addColorStop(0,'#e31b234d');g.addColorStop(1,'#e31b2300');x.beginPath();x.moveTo(pts[0][0],h-pad.b);pts.forEach(p=>x.lineTo(...p));x.lineTo(pts.at(-1)[0],h-pad.b);x.closePath();x.fillStyle=g;x.fill();x.beginPath();pts.forEach((p,i)=>i?x.lineTo(...p):x.moveTo(...p));x.strokeStyle='#e31b23';x.lineWidth=2;x.stroke();x.fillStyle='#a0a0b0';const step=Math.max(1,Math.ceil(chartLabels.length/6));chartLabels.forEach((l,i)=>{if(i%step===0||i===chartLabels.length-1)x.fillText(l.slice(5),pts[i][0]-14,h-10)})})();
