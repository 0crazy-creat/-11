import{_ as il,j as Ut,o as rl,J as sl,a as gt,c as Gt,b as Fe,n as bi,t as It,f as al,w as ol,F as ai,r as oi,d as wi,e as Er,i as yr,h as ll,u as cl,R as ul}from"./index-BOWsrkPT.js";import{i as hl}from"./index-Bb6yjXMn.js";import{o as dl,p as fl,q as pl,v as ml,l as _l}from"./index-qtbYiwhp.js";import{c as gl}from"./ws-Ca0RFHYk.js";import"./request-DAnAmEkp.js";/**
 * @license
 * Copyright 2010-2024 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const Xs="169",vl=0,ma=1,xl=2,vo=1,Ml=2,Jt=3,pn=0,Mt=1,Qt=2,dn=0,Kn=1,_a=2,ga=3,va=4,Sl=5,bn=100,El=101,yl=102,Tl=103,Al=104,bl=200,wl=201,Rl=202,Cl=203,rs=204,ss=205,Pl=206,Ll=207,Dl=208,Ul=209,Il=210,Nl=211,Fl=212,Ol=213,Bl=214,as=0,os=1,ls=2,jn=3,cs=4,us=5,hs=6,ds=7,xo=0,zl=1,Hl=2,fn=0,Gl=1,Vl=2,kl=3,Wl=4,Xl=5,ql=6,Yl=7,Mo=300,Jn=301,Qn=302,fs=303,ps=304,fr=306,ms=1e3,Rn=1001,_s=1002,Lt=1003,Kl=1004,Ri=1005,Bt=1006,Tr=1007,Cn=1008,nn=1009,So=1010,Eo=1011,vi=1012,qs=1013,Pn=1014,en=1015,xi=1016,Ys=1017,Ks=1018,ei=1020,yo=35902,To=1021,Ao=1022,Ht=1023,bo=1024,wo=1025,$n=1026,ti=1027,Ro=1028,$s=1029,Co=1030,Zs=1031,js=1033,ji=33776,Ji=33777,Qi=33778,er=33779,gs=35840,vs=35841,xs=35842,Ms=35843,Ss=36196,Es=37492,ys=37496,Ts=37808,As=37809,bs=37810,ws=37811,Rs=37812,Cs=37813,Ps=37814,Ls=37815,Ds=37816,Us=37817,Is=37818,Ns=37819,Fs=37820,Os=37821,tr=36492,Bs=36494,zs=36495,Po=36283,Hs=36284,Gs=36285,Vs=36286,$l=3200,Zl=3201,Lo=0,jl=1,hn="",Vt="srgb",_n="srgb-linear",Js="display-p3",pr="display-p3-linear",rr="linear",Qe="srgb",sr="rec709",ar="p3",Un=7680,xa=519,Jl=512,Ql=513,ec=514,Do=515,tc=516,nc=517,ic=518,rc=519,Ma=35044,Sa="300 es",tn=2e3,or=2001;class ii{addEventListener(e,t){this._listeners===void 0&&(this._listeners={});const n=this._listeners;n[e]===void 0&&(n[e]=[]),n[e].indexOf(t)===-1&&n[e].push(t)}hasEventListener(e,t){if(this._listeners===void 0)return!1;const n=this._listeners;return n[e]!==void 0&&n[e].indexOf(t)!==-1}removeEventListener(e,t){if(this._listeners===void 0)return;const r=this._listeners[e];if(r!==void 0){const s=r.indexOf(t);s!==-1&&r.splice(s,1)}}dispatchEvent(e){if(this._listeners===void 0)return;const n=this._listeners[e.type];if(n!==void 0){e.target=this;const r=n.slice(0);for(let s=0,a=r.length;s<a;s++)r[s].call(this,e);e.target=null}}}const dt=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"],Ar=Math.PI/180,ks=180/Math.PI;function Mi(){const i=Math.random()*4294967295|0,e=Math.random()*4294967295|0,t=Math.random()*4294967295|0,n=Math.random()*4294967295|0;return(dt[i&255]+dt[i>>8&255]+dt[i>>16&255]+dt[i>>24&255]+"-"+dt[e&255]+dt[e>>8&255]+"-"+dt[e>>16&15|64]+dt[e>>24&255]+"-"+dt[t&63|128]+dt[t>>8&255]+"-"+dt[t>>16&255]+dt[t>>24&255]+dt[n&255]+dt[n>>8&255]+dt[n>>16&255]+dt[n>>24&255]).toLowerCase()}function xt(i,e,t){return Math.max(e,Math.min(t,i))}function sc(i,e){return(i%e+e)%e}function br(i,e,t){return(1-t)*i+t*e}function li(i,e){switch(e.constructor){case Float32Array:return i;case Uint32Array:return i/4294967295;case Uint16Array:return i/65535;case Uint8Array:return i/255;case Int32Array:return Math.max(i/2147483647,-1);case Int16Array:return Math.max(i/32767,-1);case Int8Array:return Math.max(i/127,-1);default:throw new Error("Invalid component type.")}}function vt(i,e){switch(e.constructor){case Float32Array:return i;case Uint32Array:return Math.round(i*4294967295);case Uint16Array:return Math.round(i*65535);case Uint8Array:return Math.round(i*255);case Int32Array:return Math.round(i*2147483647);case Int16Array:return Math.round(i*32767);case Int8Array:return Math.round(i*127);default:throw new Error("Invalid component type.")}}class ze{constructor(e=0,t=0){ze.prototype.isVector2=!0,this.x=e,this.y=t}get width(){return this.x}set width(e){this.x=e}get height(){return this.y}set height(e){this.y=e}set(e,t){return this.x=e,this.y=t,this}setScalar(e){return this.x=e,this.y=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y)}copy(e){return this.x=e.x,this.y=e.y,this}add(e){return this.x+=e.x,this.y+=e.y,this}addScalar(e){return this.x+=e,this.y+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this}subScalar(e){return this.x-=e,this.y-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this}multiply(e){return this.x*=e.x,this.y*=e.y,this}multiplyScalar(e){return this.x*=e,this.y*=e,this}divide(e){return this.x/=e.x,this.y/=e.y,this}divideScalar(e){return this.multiplyScalar(1/e)}applyMatrix3(e){const t=this.x,n=this.y,r=e.elements;return this.x=r[0]*t+r[3]*n+r[6],this.y=r[1]*t+r[4]*n+r[7],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this}clamp(e,t){return this.x=Math.max(e.x,Math.min(t.x,this.x)),this.y=Math.max(e.y,Math.min(t.y,this.y)),this}clampScalar(e,t){return this.x=Math.max(e,Math.min(t,this.x)),this.y=Math.max(e,Math.min(t,this.y)),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(Math.max(e,Math.min(t,n)))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(e){return this.x*e.x+this.y*e.y}cross(e){return this.x*e.y-this.y*e.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(xt(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y;return t*t+n*n}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this}equals(e){return e.x===this.x&&e.y===this.y}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this}rotateAround(e,t){const n=Math.cos(t),r=Math.sin(t),s=this.x-e.x,a=this.y-e.y;return this.x=s*n-a*r+e.x,this.y=s*r+a*n+e.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}}class De{constructor(e,t,n,r,s,a,o,l,c){De.prototype.isMatrix3=!0,this.elements=[1,0,0,0,1,0,0,0,1],e!==void 0&&this.set(e,t,n,r,s,a,o,l,c)}set(e,t,n,r,s,a,o,l,c){const d=this.elements;return d[0]=e,d[1]=r,d[2]=o,d[3]=t,d[4]=s,d[5]=l,d[6]=n,d[7]=a,d[8]=c,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],this}extractBasis(e,t,n){return e.setFromMatrix3Column(this,0),t.setFromMatrix3Column(this,1),n.setFromMatrix3Column(this,2),this}setFromMatrix4(e){const t=e.elements;return this.set(t[0],t[4],t[8],t[1],t[5],t[9],t[2],t[6],t[10]),this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,r=t.elements,s=this.elements,a=n[0],o=n[3],l=n[6],c=n[1],d=n[4],p=n[7],f=n[2],m=n[5],_=n[8],M=r[0],h=r[3],u=r[6],y=r[1],S=r[4],T=r[7],N=r[2],R=r[5],w=r[8];return s[0]=a*M+o*y+l*N,s[3]=a*h+o*S+l*R,s[6]=a*u+o*T+l*w,s[1]=c*M+d*y+p*N,s[4]=c*h+d*S+p*R,s[7]=c*u+d*T+p*w,s[2]=f*M+m*y+_*N,s[5]=f*h+m*S+_*R,s[8]=f*u+m*T+_*w,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[3]*=e,t[6]*=e,t[1]*=e,t[4]*=e,t[7]*=e,t[2]*=e,t[5]*=e,t[8]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[1],r=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],d=e[8];return t*a*d-t*o*c-n*s*d+n*o*l+r*s*c-r*a*l}invert(){const e=this.elements,t=e[0],n=e[1],r=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],d=e[8],p=d*a-o*c,f=o*l-d*s,m=c*s-a*l,_=t*p+n*f+r*m;if(_===0)return this.set(0,0,0,0,0,0,0,0,0);const M=1/_;return e[0]=p*M,e[1]=(r*c-d*n)*M,e[2]=(o*n-r*a)*M,e[3]=f*M,e[4]=(d*t-r*l)*M,e[5]=(r*s-o*t)*M,e[6]=m*M,e[7]=(n*l-c*t)*M,e[8]=(a*t-n*s)*M,this}transpose(){let e;const t=this.elements;return e=t[1],t[1]=t[3],t[3]=e,e=t[2],t[2]=t[6],t[6]=e,e=t[5],t[5]=t[7],t[7]=e,this}getNormalMatrix(e){return this.setFromMatrix4(e).invert().transpose()}transposeIntoArray(e){const t=this.elements;return e[0]=t[0],e[1]=t[3],e[2]=t[6],e[3]=t[1],e[4]=t[4],e[5]=t[7],e[6]=t[2],e[7]=t[5],e[8]=t[8],this}setUvTransform(e,t,n,r,s,a,o){const l=Math.cos(s),c=Math.sin(s);return this.set(n*l,n*c,-n*(l*a+c*o)+a+e,-r*c,r*l,-r*(-c*a+l*o)+o+t,0,0,1),this}scale(e,t){return this.premultiply(wr.makeScale(e,t)),this}rotate(e){return this.premultiply(wr.makeRotation(-e)),this}translate(e,t){return this.premultiply(wr.makeTranslation(e,t)),this}makeTranslation(e,t){return e.isVector2?this.set(1,0,e.x,0,1,e.y,0,0,1):this.set(1,0,e,0,1,t,0,0,1),this}makeRotation(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,n,t,0,0,0,1),this}makeScale(e,t){return this.set(e,0,0,0,t,0,0,0,1),this}equals(e){const t=this.elements,n=e.elements;for(let r=0;r<9;r++)if(t[r]!==n[r])return!1;return!0}fromArray(e,t=0){for(let n=0;n<9;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e}clone(){return new this.constructor().fromArray(this.elements)}}const wr=new De;function Uo(i){for(let e=i.length-1;e>=0;--e)if(i[e]>=65535)return!0;return!1}function lr(i){return document.createElementNS("http://www.w3.org/1999/xhtml",i)}function ac(){const i=lr("canvas");return i.style.display="block",i}const Ea={};function nr(i){i in Ea||(Ea[i]=!0,console.warn(i))}function oc(i,e,t){return new Promise(function(n,r){function s(){switch(i.clientWaitSync(e,i.SYNC_FLUSH_COMMANDS_BIT,0)){case i.WAIT_FAILED:r();break;case i.TIMEOUT_EXPIRED:setTimeout(s,t);break;default:n()}}setTimeout(s,t)})}function lc(i){const e=i.elements;e[2]=.5*e[2]+.5*e[3],e[6]=.5*e[6]+.5*e[7],e[10]=.5*e[10]+.5*e[11],e[14]=.5*e[14]+.5*e[15]}function cc(i){const e=i.elements;e[11]===-1?(e[10]=-e[10]-1,e[14]=-e[14]):(e[10]=-e[10],e[14]=-e[14]+1)}const ya=new De().set(.8224621,.177538,0,.0331941,.9668058,0,.0170827,.0723974,.9105199),Ta=new De().set(1.2249401,-.2249404,0,-.0420569,1.0420571,0,-.0196376,-.0786361,1.0982735),ci={[_n]:{transfer:rr,primaries:sr,luminanceCoefficients:[.2126,.7152,.0722],toReference:i=>i,fromReference:i=>i},[Vt]:{transfer:Qe,primaries:sr,luminanceCoefficients:[.2126,.7152,.0722],toReference:i=>i.convertSRGBToLinear(),fromReference:i=>i.convertLinearToSRGB()},[pr]:{transfer:rr,primaries:ar,luminanceCoefficients:[.2289,.6917,.0793],toReference:i=>i.applyMatrix3(Ta),fromReference:i=>i.applyMatrix3(ya)},[Js]:{transfer:Qe,primaries:ar,luminanceCoefficients:[.2289,.6917,.0793],toReference:i=>i.convertSRGBToLinear().applyMatrix3(Ta),fromReference:i=>i.applyMatrix3(ya).convertLinearToSRGB()}},uc=new Set([_n,pr]),Ke={enabled:!0,_workingColorSpace:_n,get workingColorSpace(){return this._workingColorSpace},set workingColorSpace(i){if(!uc.has(i))throw new Error(`Unsupported working color space, "${i}".`);this._workingColorSpace=i},convert:function(i,e,t){if(this.enabled===!1||e===t||!e||!t)return i;const n=ci[e].toReference,r=ci[t].fromReference;return r(n(i))},fromWorkingColorSpace:function(i,e){return this.convert(i,this._workingColorSpace,e)},toWorkingColorSpace:function(i,e){return this.convert(i,e,this._workingColorSpace)},getPrimaries:function(i){return ci[i].primaries},getTransfer:function(i){return i===hn?rr:ci[i].transfer},getLuminanceCoefficients:function(i,e=this._workingColorSpace){return i.fromArray(ci[e].luminanceCoefficients)}};function Zn(i){return i<.04045?i*.0773993808:Math.pow(i*.9478672986+.0521327014,2.4)}function Rr(i){return i<.0031308?i*12.92:1.055*Math.pow(i,.41666)-.055}let In;class hc{static getDataURL(e){if(/^data:/i.test(e.src)||typeof HTMLCanvasElement>"u")return e.src;let t;if(e instanceof HTMLCanvasElement)t=e;else{In===void 0&&(In=lr("canvas")),In.width=e.width,In.height=e.height;const n=In.getContext("2d");e instanceof ImageData?n.putImageData(e,0,0):n.drawImage(e,0,0,e.width,e.height),t=In}return t.width>2048||t.height>2048?(console.warn("THREE.ImageUtils.getDataURL: Image converted to jpg for performance reasons",e),t.toDataURL("image/jpeg",.6)):t.toDataURL("image/png")}static sRGBToLinear(e){if(typeof HTMLImageElement<"u"&&e instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&e instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&e instanceof ImageBitmap){const t=lr("canvas");t.width=e.width,t.height=e.height;const n=t.getContext("2d");n.drawImage(e,0,0,e.width,e.height);const r=n.getImageData(0,0,e.width,e.height),s=r.data;for(let a=0;a<s.length;a++)s[a]=Zn(s[a]/255)*255;return n.putImageData(r,0,0),t}else if(e.data){const t=e.data.slice(0);for(let n=0;n<t.length;n++)t instanceof Uint8Array||t instanceof Uint8ClampedArray?t[n]=Math.floor(Zn(t[n]/255)*255):t[n]=Zn(t[n]);return{data:t,width:e.width,height:e.height}}else return console.warn("THREE.ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),e}}let dc=0;class Io{constructor(e=null){this.isSource=!0,Object.defineProperty(this,"id",{value:dc++}),this.uuid=Mi(),this.data=e,this.dataReady=!0,this.version=0}set needsUpdate(e){e===!0&&this.version++}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.images[this.uuid]!==void 0)return e.images[this.uuid];const n={uuid:this.uuid,url:""},r=this.data;if(r!==null){let s;if(Array.isArray(r)){s=[];for(let a=0,o=r.length;a<o;a++)r[a].isDataTexture?s.push(Cr(r[a].image)):s.push(Cr(r[a]))}else s=Cr(r);n.url=s}return t||(e.images[this.uuid]=n),n}}function Cr(i){return typeof HTMLImageElement<"u"&&i instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&i instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&i instanceof ImageBitmap?hc.getDataURL(i):i.data?{data:Array.from(i.data),width:i.width,height:i.height,type:i.data.constructor.name}:(console.warn("THREE.Texture: Unable to serialize Texture."),{})}let fc=0;class St extends ii{constructor(e=St.DEFAULT_IMAGE,t=St.DEFAULT_MAPPING,n=Rn,r=Rn,s=Bt,a=Cn,o=Ht,l=nn,c=St.DEFAULT_ANISOTROPY,d=hn){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:fc++}),this.uuid=Mi(),this.name="",this.source=new Io(e),this.mipmaps=[],this.mapping=t,this.channel=0,this.wrapS=n,this.wrapT=r,this.magFilter=s,this.minFilter=a,this.anisotropy=c,this.format=o,this.internalFormat=null,this.type=l,this.offset=new ze(0,0),this.repeat=new ze(1,1),this.center=new ze(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new De,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=d,this.userData={},this.version=0,this.onUpdate=null,this.isRenderTargetTexture=!1,this.pmremVersion=0}get image(){return this.source.data}set image(e=null){this.source.data=e}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}clone(){return new this.constructor().copy(this)}copy(e){return this.name=e.name,this.source=e.source,this.mipmaps=e.mipmaps.slice(0),this.mapping=e.mapping,this.channel=e.channel,this.wrapS=e.wrapS,this.wrapT=e.wrapT,this.magFilter=e.magFilter,this.minFilter=e.minFilter,this.anisotropy=e.anisotropy,this.format=e.format,this.internalFormat=e.internalFormat,this.type=e.type,this.offset.copy(e.offset),this.repeat.copy(e.repeat),this.center.copy(e.center),this.rotation=e.rotation,this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrix.copy(e.matrix),this.generateMipmaps=e.generateMipmaps,this.premultiplyAlpha=e.premultiplyAlpha,this.flipY=e.flipY,this.unpackAlignment=e.unpackAlignment,this.colorSpace=e.colorSpace,this.userData=JSON.parse(JSON.stringify(e.userData)),this.needsUpdate=!0,this}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.textures[this.uuid]!==void 0)return e.textures[this.uuid];const n={metadata:{version:4.6,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(e).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(n.userData=this.userData),t||(e.textures[this.uuid]=n),n}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(e){if(this.mapping!==Mo)return e;if(e.applyMatrix3(this.matrix),e.x<0||e.x>1)switch(this.wrapS){case ms:e.x=e.x-Math.floor(e.x);break;case Rn:e.x=e.x<0?0:1;break;case _s:Math.abs(Math.floor(e.x)%2)===1?e.x=Math.ceil(e.x)-e.x:e.x=e.x-Math.floor(e.x);break}if(e.y<0||e.y>1)switch(this.wrapT){case ms:e.y=e.y-Math.floor(e.y);break;case Rn:e.y=e.y<0?0:1;break;case _s:Math.abs(Math.floor(e.y)%2)===1?e.y=Math.ceil(e.y)-e.y:e.y=e.y-Math.floor(e.y);break}return this.flipY&&(e.y=1-e.y),e}set needsUpdate(e){e===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(e){e===!0&&this.pmremVersion++}}St.DEFAULT_IMAGE=null;St.DEFAULT_MAPPING=Mo;St.DEFAULT_ANISOTROPY=1;class Ze{constructor(e=0,t=0,n=0,r=1){Ze.prototype.isVector4=!0,this.x=e,this.y=t,this.z=n,this.w=r}get width(){return this.z}set width(e){this.z=e}get height(){return this.w}set height(e){this.w=e}set(e,t,n,r){return this.x=e,this.y=t,this.z=n,this.w=r,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this.w=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setW(e){return this.w=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;case 3:this.w=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this.w=e.w!==void 0?e.w:1,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this.w+=e.w,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this.w+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this.w=e.w+t.w,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this.w+=e.w*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this.w-=e.w,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this.w-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this.w=e.w-t.w,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this.w*=e.w,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this.w*=e,this}applyMatrix4(e){const t=this.x,n=this.y,r=this.z,s=this.w,a=e.elements;return this.x=a[0]*t+a[4]*n+a[8]*r+a[12]*s,this.y=a[1]*t+a[5]*n+a[9]*r+a[13]*s,this.z=a[2]*t+a[6]*n+a[10]*r+a[14]*s,this.w=a[3]*t+a[7]*n+a[11]*r+a[15]*s,this}divideScalar(e){return this.multiplyScalar(1/e)}setAxisAngleFromQuaternion(e){this.w=2*Math.acos(e.w);const t=Math.sqrt(1-e.w*e.w);return t<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=e.x/t,this.y=e.y/t,this.z=e.z/t),this}setAxisAngleFromRotationMatrix(e){let t,n,r,s;const l=e.elements,c=l[0],d=l[4],p=l[8],f=l[1],m=l[5],_=l[9],M=l[2],h=l[6],u=l[10];if(Math.abs(d-f)<.01&&Math.abs(p-M)<.01&&Math.abs(_-h)<.01){if(Math.abs(d+f)<.1&&Math.abs(p+M)<.1&&Math.abs(_+h)<.1&&Math.abs(c+m+u-3)<.1)return this.set(1,0,0,0),this;t=Math.PI;const S=(c+1)/2,T=(m+1)/2,N=(u+1)/2,R=(d+f)/4,w=(p+M)/4,I=(_+h)/4;return S>T&&S>N?S<.01?(n=0,r=.707106781,s=.707106781):(n=Math.sqrt(S),r=R/n,s=w/n):T>N?T<.01?(n=.707106781,r=0,s=.707106781):(r=Math.sqrt(T),n=R/r,s=I/r):N<.01?(n=.707106781,r=.707106781,s=0):(s=Math.sqrt(N),n=w/s,r=I/s),this.set(n,r,s,t),this}let y=Math.sqrt((h-_)*(h-_)+(p-M)*(p-M)+(f-d)*(f-d));return Math.abs(y)<.001&&(y=1),this.x=(h-_)/y,this.y=(p-M)/y,this.z=(f-d)/y,this.w=Math.acos((c+m+u-1)/2),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this.w=t[15],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this.w=Math.min(this.w,e.w),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this.w=Math.max(this.w,e.w),this}clamp(e,t){return this.x=Math.max(e.x,Math.min(t.x,this.x)),this.y=Math.max(e.y,Math.min(t.y,this.y)),this.z=Math.max(e.z,Math.min(t.z,this.z)),this.w=Math.max(e.w,Math.min(t.w,this.w)),this}clampScalar(e,t){return this.x=Math.max(e,Math.min(t,this.x)),this.y=Math.max(e,Math.min(t,this.y)),this.z=Math.max(e,Math.min(t,this.z)),this.w=Math.max(e,Math.min(t,this.w)),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(Math.max(e,Math.min(t,n)))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z+this.w*e.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this.w+=(e.w-this.w)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this.w=e.w+(t.w-e.w)*n,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z&&e.w===this.w}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this.w=e[t+3],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e[t+3]=this.w,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this.w=e.getW(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}}class pc extends ii{constructor(e=1,t=1,n={}){super(),this.isRenderTarget=!0,this.width=e,this.height=t,this.depth=1,this.scissor=new Ze(0,0,e,t),this.scissorTest=!1,this.viewport=new Ze(0,0,e,t);const r={width:e,height:t,depth:1};n=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:Bt,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1},n);const s=new St(r,n.mapping,n.wrapS,n.wrapT,n.magFilter,n.minFilter,n.format,n.type,n.anisotropy,n.colorSpace);s.flipY=!1,s.generateMipmaps=n.generateMipmaps,s.internalFormat=n.internalFormat,this.textures=[];const a=n.count;for(let o=0;o<a;o++)this.textures[o]=s.clone(),this.textures[o].isRenderTargetTexture=!0;this.depthBuffer=n.depthBuffer,this.stencilBuffer=n.stencilBuffer,this.resolveDepthBuffer=n.resolveDepthBuffer,this.resolveStencilBuffer=n.resolveStencilBuffer,this.depthTexture=n.depthTexture,this.samples=n.samples}get texture(){return this.textures[0]}set texture(e){this.textures[0]=e}setSize(e,t,n=1){if(this.width!==e||this.height!==t||this.depth!==n){this.width=e,this.height=t,this.depth=n;for(let r=0,s=this.textures.length;r<s;r++)this.textures[r].image.width=e,this.textures[r].image.height=t,this.textures[r].image.depth=n;this.dispose()}this.viewport.set(0,0,e,t),this.scissor.set(0,0,e,t)}clone(){return new this.constructor().copy(this)}copy(e){this.width=e.width,this.height=e.height,this.depth=e.depth,this.scissor.copy(e.scissor),this.scissorTest=e.scissorTest,this.viewport.copy(e.viewport),this.textures.length=0;for(let n=0,r=e.textures.length;n<r;n++)this.textures[n]=e.textures[n].clone(),this.textures[n].isRenderTargetTexture=!0;const t=Object.assign({},e.texture.image);return this.texture.source=new Io(t),this.depthBuffer=e.depthBuffer,this.stencilBuffer=e.stencilBuffer,this.resolveDepthBuffer=e.resolveDepthBuffer,this.resolveStencilBuffer=e.resolveStencilBuffer,e.depthTexture!==null&&(this.depthTexture=e.depthTexture.clone()),this.samples=e.samples,this}dispose(){this.dispatchEvent({type:"dispose"})}}class Ln extends pc{constructor(e=1,t=1,n={}){super(e,t,n),this.isWebGLRenderTarget=!0}}class No extends St{constructor(e=null,t=1,n=1,r=1){super(null),this.isDataArrayTexture=!0,this.image={data:e,width:t,height:n,depth:r},this.magFilter=Lt,this.minFilter=Lt,this.wrapR=Rn,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(e){this.layerUpdates.add(e)}clearLayerUpdates(){this.layerUpdates.clear()}}class mc extends St{constructor(e=null,t=1,n=1,r=1){super(null),this.isData3DTexture=!0,this.image={data:e,width:t,height:n,depth:r},this.magFilter=Lt,this.minFilter=Lt,this.wrapR=Rn,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}class Si{constructor(e=0,t=0,n=0,r=1){this.isQuaternion=!0,this._x=e,this._y=t,this._z=n,this._w=r}static slerpFlat(e,t,n,r,s,a,o){let l=n[r+0],c=n[r+1],d=n[r+2],p=n[r+3];const f=s[a+0],m=s[a+1],_=s[a+2],M=s[a+3];if(o===0){e[t+0]=l,e[t+1]=c,e[t+2]=d,e[t+3]=p;return}if(o===1){e[t+0]=f,e[t+1]=m,e[t+2]=_,e[t+3]=M;return}if(p!==M||l!==f||c!==m||d!==_){let h=1-o;const u=l*f+c*m+d*_+p*M,y=u>=0?1:-1,S=1-u*u;if(S>Number.EPSILON){const N=Math.sqrt(S),R=Math.atan2(N,u*y);h=Math.sin(h*R)/N,o=Math.sin(o*R)/N}const T=o*y;if(l=l*h+f*T,c=c*h+m*T,d=d*h+_*T,p=p*h+M*T,h===1-o){const N=1/Math.sqrt(l*l+c*c+d*d+p*p);l*=N,c*=N,d*=N,p*=N}}e[t]=l,e[t+1]=c,e[t+2]=d,e[t+3]=p}static multiplyQuaternionsFlat(e,t,n,r,s,a){const o=n[r],l=n[r+1],c=n[r+2],d=n[r+3],p=s[a],f=s[a+1],m=s[a+2],_=s[a+3];return e[t]=o*_+d*p+l*m-c*f,e[t+1]=l*_+d*f+c*p-o*m,e[t+2]=c*_+d*m+o*f-l*p,e[t+3]=d*_-o*p-l*f-c*m,e}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get w(){return this._w}set w(e){this._w=e,this._onChangeCallback()}set(e,t,n,r){return this._x=e,this._y=t,this._z=n,this._w=r,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(e){return this._x=e.x,this._y=e.y,this._z=e.z,this._w=e.w,this._onChangeCallback(),this}setFromEuler(e,t=!0){const n=e._x,r=e._y,s=e._z,a=e._order,o=Math.cos,l=Math.sin,c=o(n/2),d=o(r/2),p=o(s/2),f=l(n/2),m=l(r/2),_=l(s/2);switch(a){case"XYZ":this._x=f*d*p+c*m*_,this._y=c*m*p-f*d*_,this._z=c*d*_+f*m*p,this._w=c*d*p-f*m*_;break;case"YXZ":this._x=f*d*p+c*m*_,this._y=c*m*p-f*d*_,this._z=c*d*_-f*m*p,this._w=c*d*p+f*m*_;break;case"ZXY":this._x=f*d*p-c*m*_,this._y=c*m*p+f*d*_,this._z=c*d*_+f*m*p,this._w=c*d*p-f*m*_;break;case"ZYX":this._x=f*d*p-c*m*_,this._y=c*m*p+f*d*_,this._z=c*d*_-f*m*p,this._w=c*d*p+f*m*_;break;case"YZX":this._x=f*d*p+c*m*_,this._y=c*m*p+f*d*_,this._z=c*d*_-f*m*p,this._w=c*d*p-f*m*_;break;case"XZY":this._x=f*d*p-c*m*_,this._y=c*m*p-f*d*_,this._z=c*d*_+f*m*p,this._w=c*d*p+f*m*_;break;default:console.warn("THREE.Quaternion: .setFromEuler() encountered an unknown order: "+a)}return t===!0&&this._onChangeCallback(),this}setFromAxisAngle(e,t){const n=t/2,r=Math.sin(n);return this._x=e.x*r,this._y=e.y*r,this._z=e.z*r,this._w=Math.cos(n),this._onChangeCallback(),this}setFromRotationMatrix(e){const t=e.elements,n=t[0],r=t[4],s=t[8],a=t[1],o=t[5],l=t[9],c=t[2],d=t[6],p=t[10],f=n+o+p;if(f>0){const m=.5/Math.sqrt(f+1);this._w=.25/m,this._x=(d-l)*m,this._y=(s-c)*m,this._z=(a-r)*m}else if(n>o&&n>p){const m=2*Math.sqrt(1+n-o-p);this._w=(d-l)/m,this._x=.25*m,this._y=(r+a)/m,this._z=(s+c)/m}else if(o>p){const m=2*Math.sqrt(1+o-n-p);this._w=(s-c)/m,this._x=(r+a)/m,this._y=.25*m,this._z=(l+d)/m}else{const m=2*Math.sqrt(1+p-n-o);this._w=(a-r)/m,this._x=(s+c)/m,this._y=(l+d)/m,this._z=.25*m}return this._onChangeCallback(),this}setFromUnitVectors(e,t){let n=e.dot(t)+1;return n<Number.EPSILON?(n=0,Math.abs(e.x)>Math.abs(e.z)?(this._x=-e.y,this._y=e.x,this._z=0,this._w=n):(this._x=0,this._y=-e.z,this._z=e.y,this._w=n)):(this._x=e.y*t.z-e.z*t.y,this._y=e.z*t.x-e.x*t.z,this._z=e.x*t.y-e.y*t.x,this._w=n),this.normalize()}angleTo(e){return 2*Math.acos(Math.abs(xt(this.dot(e),-1,1)))}rotateTowards(e,t){const n=this.angleTo(e);if(n===0)return this;const r=Math.min(1,t/n);return this.slerp(e,r),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(e){return this._x*e._x+this._y*e._y+this._z*e._z+this._w*e._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let e=this.length();return e===0?(this._x=0,this._y=0,this._z=0,this._w=1):(e=1/e,this._x=this._x*e,this._y=this._y*e,this._z=this._z*e,this._w=this._w*e),this._onChangeCallback(),this}multiply(e){return this.multiplyQuaternions(this,e)}premultiply(e){return this.multiplyQuaternions(e,this)}multiplyQuaternions(e,t){const n=e._x,r=e._y,s=e._z,a=e._w,o=t._x,l=t._y,c=t._z,d=t._w;return this._x=n*d+a*o+r*c-s*l,this._y=r*d+a*l+s*o-n*c,this._z=s*d+a*c+n*l-r*o,this._w=a*d-n*o-r*l-s*c,this._onChangeCallback(),this}slerp(e,t){if(t===0)return this;if(t===1)return this.copy(e);const n=this._x,r=this._y,s=this._z,a=this._w;let o=a*e._w+n*e._x+r*e._y+s*e._z;if(o<0?(this._w=-e._w,this._x=-e._x,this._y=-e._y,this._z=-e._z,o=-o):this.copy(e),o>=1)return this._w=a,this._x=n,this._y=r,this._z=s,this;const l=1-o*o;if(l<=Number.EPSILON){const m=1-t;return this._w=m*a+t*this._w,this._x=m*n+t*this._x,this._y=m*r+t*this._y,this._z=m*s+t*this._z,this.normalize(),this}const c=Math.sqrt(l),d=Math.atan2(c,o),p=Math.sin((1-t)*d)/c,f=Math.sin(t*d)/c;return this._w=a*p+this._w*f,this._x=n*p+this._x*f,this._y=r*p+this._y*f,this._z=s*p+this._z*f,this._onChangeCallback(),this}slerpQuaternions(e,t,n){return this.copy(e).slerp(t,n)}random(){const e=2*Math.PI*Math.random(),t=2*Math.PI*Math.random(),n=Math.random(),r=Math.sqrt(1-n),s=Math.sqrt(n);return this.set(r*Math.sin(e),r*Math.cos(e),s*Math.sin(t),s*Math.cos(t))}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._w===this._w}fromArray(e,t=0){return this._x=e[t],this._y=e[t+1],this._z=e[t+2],this._w=e[t+3],this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._w,e}fromBufferAttribute(e,t){return this._x=e.getX(t),this._y=e.getY(t),this._z=e.getZ(t),this._w=e.getW(t),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}class D{constructor(e=0,t=0,n=0){D.prototype.isVector3=!0,this.x=e,this.y=t,this.z=n}set(e,t,n){return n===void 0&&(n=this.z),this.x=e,this.y=t,this.z=n,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this}multiplyVectors(e,t){return this.x=e.x*t.x,this.y=e.y*t.y,this.z=e.z*t.z,this}applyEuler(e){return this.applyQuaternion(Aa.setFromEuler(e))}applyAxisAngle(e,t){return this.applyQuaternion(Aa.setFromAxisAngle(e,t))}applyMatrix3(e){const t=this.x,n=this.y,r=this.z,s=e.elements;return this.x=s[0]*t+s[3]*n+s[6]*r,this.y=s[1]*t+s[4]*n+s[7]*r,this.z=s[2]*t+s[5]*n+s[8]*r,this}applyNormalMatrix(e){return this.applyMatrix3(e).normalize()}applyMatrix4(e){const t=this.x,n=this.y,r=this.z,s=e.elements,a=1/(s[3]*t+s[7]*n+s[11]*r+s[15]);return this.x=(s[0]*t+s[4]*n+s[8]*r+s[12])*a,this.y=(s[1]*t+s[5]*n+s[9]*r+s[13])*a,this.z=(s[2]*t+s[6]*n+s[10]*r+s[14])*a,this}applyQuaternion(e){const t=this.x,n=this.y,r=this.z,s=e.x,a=e.y,o=e.z,l=e.w,c=2*(a*r-o*n),d=2*(o*t-s*r),p=2*(s*n-a*t);return this.x=t+l*c+a*p-o*d,this.y=n+l*d+o*c-s*p,this.z=r+l*p+s*d-a*c,this}project(e){return this.applyMatrix4(e.matrixWorldInverse).applyMatrix4(e.projectionMatrix)}unproject(e){return this.applyMatrix4(e.projectionMatrixInverse).applyMatrix4(e.matrixWorld)}transformDirection(e){const t=this.x,n=this.y,r=this.z,s=e.elements;return this.x=s[0]*t+s[4]*n+s[8]*r,this.y=s[1]*t+s[5]*n+s[9]*r,this.z=s[2]*t+s[6]*n+s[10]*r,this.normalize()}divide(e){return this.x/=e.x,this.y/=e.y,this.z/=e.z,this}divideScalar(e){return this.multiplyScalar(1/e)}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this}clamp(e,t){return this.x=Math.max(e.x,Math.min(t.x,this.x)),this.y=Math.max(e.y,Math.min(t.y,this.y)),this.z=Math.max(e.z,Math.min(t.z,this.z)),this}clampScalar(e,t){return this.x=Math.max(e,Math.min(t,this.x)),this.y=Math.max(e,Math.min(t,this.y)),this.z=Math.max(e,Math.min(t,this.z)),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(Math.max(e,Math.min(t,n)))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this}cross(e){return this.crossVectors(this,e)}crossVectors(e,t){const n=e.x,r=e.y,s=e.z,a=t.x,o=t.y,l=t.z;return this.x=r*l-s*o,this.y=s*a-n*l,this.z=n*o-r*a,this}projectOnVector(e){const t=e.lengthSq();if(t===0)return this.set(0,0,0);const n=e.dot(this)/t;return this.copy(e).multiplyScalar(n)}projectOnPlane(e){return Pr.copy(this).projectOnVector(e),this.sub(Pr)}reflect(e){return this.sub(Pr.copy(e).multiplyScalar(2*this.dot(e)))}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(xt(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y,r=this.z-e.z;return t*t+n*n+r*r}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)+Math.abs(this.z-e.z)}setFromSpherical(e){return this.setFromSphericalCoords(e.radius,e.phi,e.theta)}setFromSphericalCoords(e,t,n){const r=Math.sin(t)*e;return this.x=r*Math.sin(n),this.y=Math.cos(t)*e,this.z=r*Math.cos(n),this}setFromCylindrical(e){return this.setFromCylindricalCoords(e.radius,e.theta,e.y)}setFromCylindricalCoords(e,t,n){return this.x=e*Math.sin(t),this.y=n,this.z=e*Math.cos(t),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this}setFromMatrixScale(e){const t=this.setFromMatrixColumn(e,0).length(),n=this.setFromMatrixColumn(e,1).length(),r=this.setFromMatrixColumn(e,2).length();return this.x=t,this.y=n,this.z=r,this}setFromMatrixColumn(e,t){return this.fromArray(e.elements,t*4)}setFromMatrix3Column(e,t){return this.fromArray(e.elements,t*3)}setFromEuler(e){return this.x=e._x,this.y=e._y,this.z=e._z,this}setFromColor(e){return this.x=e.r,this.y=e.g,this.z=e.b,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const e=Math.random()*Math.PI*2,t=Math.random()*2-1,n=Math.sqrt(1-t*t);return this.x=n*Math.cos(e),this.y=t,this.z=n*Math.sin(e),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}}const Pr=new D,Aa=new Si;class Ei{constructor(e=new D(1/0,1/0,1/0),t=new D(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=e,this.max=t}set(e,t){return this.min.copy(e),this.max.copy(t),this}setFromArray(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t+=3)this.expandByPoint(Nt.fromArray(e,t));return this}setFromBufferAttribute(e){this.makeEmpty();for(let t=0,n=e.count;t<n;t++)this.expandByPoint(Nt.fromBufferAttribute(e,t));return this}setFromPoints(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t++)this.expandByPoint(e[t]);return this}setFromCenterAndSize(e,t){const n=Nt.copy(t).multiplyScalar(.5);return this.min.copy(e).sub(n),this.max.copy(e).add(n),this}setFromObject(e,t=!1){return this.makeEmpty(),this.expandByObject(e,t)}clone(){return new this.constructor().copy(this)}copy(e){return this.min.copy(e.min),this.max.copy(e.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(e){return this.isEmpty()?e.set(0,0,0):e.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(e){return this.isEmpty()?e.set(0,0,0):e.subVectors(this.max,this.min)}expandByPoint(e){return this.min.min(e),this.max.max(e),this}expandByVector(e){return this.min.sub(e),this.max.add(e),this}expandByScalar(e){return this.min.addScalar(-e),this.max.addScalar(e),this}expandByObject(e,t=!1){e.updateWorldMatrix(!1,!1);const n=e.geometry;if(n!==void 0){const s=n.getAttribute("position");if(t===!0&&s!==void 0&&e.isInstancedMesh!==!0)for(let a=0,o=s.count;a<o;a++)e.isMesh===!0?e.getVertexPosition(a,Nt):Nt.fromBufferAttribute(s,a),Nt.applyMatrix4(e.matrixWorld),this.expandByPoint(Nt);else e.boundingBox!==void 0?(e.boundingBox===null&&e.computeBoundingBox(),Ci.copy(e.boundingBox)):(n.boundingBox===null&&n.computeBoundingBox(),Ci.copy(n.boundingBox)),Ci.applyMatrix4(e.matrixWorld),this.union(Ci)}const r=e.children;for(let s=0,a=r.length;s<a;s++)this.expandByObject(r[s],t);return this}containsPoint(e){return e.x>=this.min.x&&e.x<=this.max.x&&e.y>=this.min.y&&e.y<=this.max.y&&e.z>=this.min.z&&e.z<=this.max.z}containsBox(e){return this.min.x<=e.min.x&&e.max.x<=this.max.x&&this.min.y<=e.min.y&&e.max.y<=this.max.y&&this.min.z<=e.min.z&&e.max.z<=this.max.z}getParameter(e,t){return t.set((e.x-this.min.x)/(this.max.x-this.min.x),(e.y-this.min.y)/(this.max.y-this.min.y),(e.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(e){return e.max.x>=this.min.x&&e.min.x<=this.max.x&&e.max.y>=this.min.y&&e.min.y<=this.max.y&&e.max.z>=this.min.z&&e.min.z<=this.max.z}intersectsSphere(e){return this.clampPoint(e.center,Nt),Nt.distanceToSquared(e.center)<=e.radius*e.radius}intersectsPlane(e){let t,n;return e.normal.x>0?(t=e.normal.x*this.min.x,n=e.normal.x*this.max.x):(t=e.normal.x*this.max.x,n=e.normal.x*this.min.x),e.normal.y>0?(t+=e.normal.y*this.min.y,n+=e.normal.y*this.max.y):(t+=e.normal.y*this.max.y,n+=e.normal.y*this.min.y),e.normal.z>0?(t+=e.normal.z*this.min.z,n+=e.normal.z*this.max.z):(t+=e.normal.z*this.max.z,n+=e.normal.z*this.min.z),t<=-e.constant&&n>=-e.constant}intersectsTriangle(e){if(this.isEmpty())return!1;this.getCenter(ui),Pi.subVectors(this.max,ui),Nn.subVectors(e.a,ui),Fn.subVectors(e.b,ui),On.subVectors(e.c,ui),sn.subVectors(Fn,Nn),an.subVectors(On,Fn),vn.subVectors(Nn,On);let t=[0,-sn.z,sn.y,0,-an.z,an.y,0,-vn.z,vn.y,sn.z,0,-sn.x,an.z,0,-an.x,vn.z,0,-vn.x,-sn.y,sn.x,0,-an.y,an.x,0,-vn.y,vn.x,0];return!Lr(t,Nn,Fn,On,Pi)||(t=[1,0,0,0,1,0,0,0,1],!Lr(t,Nn,Fn,On,Pi))?!1:(Li.crossVectors(sn,an),t=[Li.x,Li.y,Li.z],Lr(t,Nn,Fn,On,Pi))}clampPoint(e,t){return t.copy(e).clamp(this.min,this.max)}distanceToPoint(e){return this.clampPoint(e,Nt).distanceTo(e)}getBoundingSphere(e){return this.isEmpty()?e.makeEmpty():(this.getCenter(e.center),e.radius=this.getSize(Nt).length()*.5),e}intersect(e){return this.min.max(e.min),this.max.min(e.max),this.isEmpty()&&this.makeEmpty(),this}union(e){return this.min.min(e.min),this.max.max(e.max),this}applyMatrix4(e){return this.isEmpty()?this:(Yt[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(e),Yt[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(e),Yt[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(e),Yt[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(e),Yt[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(e),Yt[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(e),Yt[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(e),Yt[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(e),this.setFromPoints(Yt),this)}translate(e){return this.min.add(e),this.max.add(e),this}equals(e){return e.min.equals(this.min)&&e.max.equals(this.max)}}const Yt=[new D,new D,new D,new D,new D,new D,new D,new D],Nt=new D,Ci=new Ei,Nn=new D,Fn=new D,On=new D,sn=new D,an=new D,vn=new D,ui=new D,Pi=new D,Li=new D,xn=new D;function Lr(i,e,t,n,r){for(let s=0,a=i.length-3;s<=a;s+=3){xn.fromArray(i,s);const o=r.x*Math.abs(xn.x)+r.y*Math.abs(xn.y)+r.z*Math.abs(xn.z),l=e.dot(xn),c=t.dot(xn),d=n.dot(xn);if(Math.max(-Math.max(l,c,d),Math.min(l,c,d))>o)return!1}return!0}const _c=new Ei,hi=new D,Dr=new D;class mr{constructor(e=new D,t=-1){this.isSphere=!0,this.center=e,this.radius=t}set(e,t){return this.center.copy(e),this.radius=t,this}setFromPoints(e,t){const n=this.center;t!==void 0?n.copy(t):_c.setFromPoints(e).getCenter(n);let r=0;for(let s=0,a=e.length;s<a;s++)r=Math.max(r,n.distanceToSquared(e[s]));return this.radius=Math.sqrt(r),this}copy(e){return this.center.copy(e.center),this.radius=e.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(e){return e.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(e){return e.distanceTo(this.center)-this.radius}intersectsSphere(e){const t=this.radius+e.radius;return e.center.distanceToSquared(this.center)<=t*t}intersectsBox(e){return e.intersectsSphere(this)}intersectsPlane(e){return Math.abs(e.distanceToPoint(this.center))<=this.radius}clampPoint(e,t){const n=this.center.distanceToSquared(e);return t.copy(e),n>this.radius*this.radius&&(t.sub(this.center).normalize(),t.multiplyScalar(this.radius).add(this.center)),t}getBoundingBox(e){return this.isEmpty()?(e.makeEmpty(),e):(e.set(this.center,this.center),e.expandByScalar(this.radius),e)}applyMatrix4(e){return this.center.applyMatrix4(e),this.radius=this.radius*e.getMaxScaleOnAxis(),this}translate(e){return this.center.add(e),this}expandByPoint(e){if(this.isEmpty())return this.center.copy(e),this.radius=0,this;hi.subVectors(e,this.center);const t=hi.lengthSq();if(t>this.radius*this.radius){const n=Math.sqrt(t),r=(n-this.radius)*.5;this.center.addScaledVector(hi,r/n),this.radius+=r}return this}union(e){return e.isEmpty()?this:this.isEmpty()?(this.copy(e),this):(this.center.equals(e.center)===!0?this.radius=Math.max(this.radius,e.radius):(Dr.subVectors(e.center,this.center).setLength(e.radius),this.expandByPoint(hi.copy(e.center).add(Dr)),this.expandByPoint(hi.copy(e.center).sub(Dr))),this)}equals(e){return e.center.equals(this.center)&&e.radius===this.radius}clone(){return new this.constructor().copy(this)}}const Kt=new D,Ur=new D,Di=new D,on=new D,Ir=new D,Ui=new D,Nr=new D;class Fo{constructor(e=new D,t=new D(0,0,-1)){this.origin=e,this.direction=t}set(e,t){return this.origin.copy(e),this.direction.copy(t),this}copy(e){return this.origin.copy(e.origin),this.direction.copy(e.direction),this}at(e,t){return t.copy(this.origin).addScaledVector(this.direction,e)}lookAt(e){return this.direction.copy(e).sub(this.origin).normalize(),this}recast(e){return this.origin.copy(this.at(e,Kt)),this}closestPointToPoint(e,t){t.subVectors(e,this.origin);const n=t.dot(this.direction);return n<0?t.copy(this.origin):t.copy(this.origin).addScaledVector(this.direction,n)}distanceToPoint(e){return Math.sqrt(this.distanceSqToPoint(e))}distanceSqToPoint(e){const t=Kt.subVectors(e,this.origin).dot(this.direction);return t<0?this.origin.distanceToSquared(e):(Kt.copy(this.origin).addScaledVector(this.direction,t),Kt.distanceToSquared(e))}distanceSqToSegment(e,t,n,r){Ur.copy(e).add(t).multiplyScalar(.5),Di.copy(t).sub(e).normalize(),on.copy(this.origin).sub(Ur);const s=e.distanceTo(t)*.5,a=-this.direction.dot(Di),o=on.dot(this.direction),l=-on.dot(Di),c=on.lengthSq(),d=Math.abs(1-a*a);let p,f,m,_;if(d>0)if(p=a*l-o,f=a*o-l,_=s*d,p>=0)if(f>=-_)if(f<=_){const M=1/d;p*=M,f*=M,m=p*(p+a*f+2*o)+f*(a*p+f+2*l)+c}else f=s,p=Math.max(0,-(a*f+o)),m=-p*p+f*(f+2*l)+c;else f=-s,p=Math.max(0,-(a*f+o)),m=-p*p+f*(f+2*l)+c;else f<=-_?(p=Math.max(0,-(-a*s+o)),f=p>0?-s:Math.min(Math.max(-s,-l),s),m=-p*p+f*(f+2*l)+c):f<=_?(p=0,f=Math.min(Math.max(-s,-l),s),m=f*(f+2*l)+c):(p=Math.max(0,-(a*s+o)),f=p>0?s:Math.min(Math.max(-s,-l),s),m=-p*p+f*(f+2*l)+c);else f=a>0?-s:s,p=Math.max(0,-(a*f+o)),m=-p*p+f*(f+2*l)+c;return n&&n.copy(this.origin).addScaledVector(this.direction,p),r&&r.copy(Ur).addScaledVector(Di,f),m}intersectSphere(e,t){Kt.subVectors(e.center,this.origin);const n=Kt.dot(this.direction),r=Kt.dot(Kt)-n*n,s=e.radius*e.radius;if(r>s)return null;const a=Math.sqrt(s-r),o=n-a,l=n+a;return l<0?null:o<0?this.at(l,t):this.at(o,t)}intersectsSphere(e){return this.distanceSqToPoint(e.center)<=e.radius*e.radius}distanceToPlane(e){const t=e.normal.dot(this.direction);if(t===0)return e.distanceToPoint(this.origin)===0?0:null;const n=-(this.origin.dot(e.normal)+e.constant)/t;return n>=0?n:null}intersectPlane(e,t){const n=this.distanceToPlane(e);return n===null?null:this.at(n,t)}intersectsPlane(e){const t=e.distanceToPoint(this.origin);return t===0||e.normal.dot(this.direction)*t<0}intersectBox(e,t){let n,r,s,a,o,l;const c=1/this.direction.x,d=1/this.direction.y,p=1/this.direction.z,f=this.origin;return c>=0?(n=(e.min.x-f.x)*c,r=(e.max.x-f.x)*c):(n=(e.max.x-f.x)*c,r=(e.min.x-f.x)*c),d>=0?(s=(e.min.y-f.y)*d,a=(e.max.y-f.y)*d):(s=(e.max.y-f.y)*d,a=(e.min.y-f.y)*d),n>a||s>r||((s>n||isNaN(n))&&(n=s),(a<r||isNaN(r))&&(r=a),p>=0?(o=(e.min.z-f.z)*p,l=(e.max.z-f.z)*p):(o=(e.max.z-f.z)*p,l=(e.min.z-f.z)*p),n>l||o>r)||((o>n||n!==n)&&(n=o),(l<r||r!==r)&&(r=l),r<0)?null:this.at(n>=0?n:r,t)}intersectsBox(e){return this.intersectBox(e,Kt)!==null}intersectTriangle(e,t,n,r,s){Ir.subVectors(t,e),Ui.subVectors(n,e),Nr.crossVectors(Ir,Ui);let a=this.direction.dot(Nr),o;if(a>0){if(r)return null;o=1}else if(a<0)o=-1,a=-a;else return null;on.subVectors(this.origin,e);const l=o*this.direction.dot(Ui.crossVectors(on,Ui));if(l<0)return null;const c=o*this.direction.dot(Ir.cross(on));if(c<0||l+c>a)return null;const d=-o*on.dot(Nr);return d<0?null:this.at(d/a,s)}applyMatrix4(e){return this.origin.applyMatrix4(e),this.direction.transformDirection(e),this}equals(e){return e.origin.equals(this.origin)&&e.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class et{constructor(e,t,n,r,s,a,o,l,c,d,p,f,m,_,M,h){et.prototype.isMatrix4=!0,this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],e!==void 0&&this.set(e,t,n,r,s,a,o,l,c,d,p,f,m,_,M,h)}set(e,t,n,r,s,a,o,l,c,d,p,f,m,_,M,h){const u=this.elements;return u[0]=e,u[4]=t,u[8]=n,u[12]=r,u[1]=s,u[5]=a,u[9]=o,u[13]=l,u[2]=c,u[6]=d,u[10]=p,u[14]=f,u[3]=m,u[7]=_,u[11]=M,u[15]=h,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new et().fromArray(this.elements)}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],t[9]=n[9],t[10]=n[10],t[11]=n[11],t[12]=n[12],t[13]=n[13],t[14]=n[14],t[15]=n[15],this}copyPosition(e){const t=this.elements,n=e.elements;return t[12]=n[12],t[13]=n[13],t[14]=n[14],this}setFromMatrix3(e){const t=e.elements;return this.set(t[0],t[3],t[6],0,t[1],t[4],t[7],0,t[2],t[5],t[8],0,0,0,0,1),this}extractBasis(e,t,n){return e.setFromMatrixColumn(this,0),t.setFromMatrixColumn(this,1),n.setFromMatrixColumn(this,2),this}makeBasis(e,t,n){return this.set(e.x,t.x,n.x,0,e.y,t.y,n.y,0,e.z,t.z,n.z,0,0,0,0,1),this}extractRotation(e){const t=this.elements,n=e.elements,r=1/Bn.setFromMatrixColumn(e,0).length(),s=1/Bn.setFromMatrixColumn(e,1).length(),a=1/Bn.setFromMatrixColumn(e,2).length();return t[0]=n[0]*r,t[1]=n[1]*r,t[2]=n[2]*r,t[3]=0,t[4]=n[4]*s,t[5]=n[5]*s,t[6]=n[6]*s,t[7]=0,t[8]=n[8]*a,t[9]=n[9]*a,t[10]=n[10]*a,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromEuler(e){const t=this.elements,n=e.x,r=e.y,s=e.z,a=Math.cos(n),o=Math.sin(n),l=Math.cos(r),c=Math.sin(r),d=Math.cos(s),p=Math.sin(s);if(e.order==="XYZ"){const f=a*d,m=a*p,_=o*d,M=o*p;t[0]=l*d,t[4]=-l*p,t[8]=c,t[1]=m+_*c,t[5]=f-M*c,t[9]=-o*l,t[2]=M-f*c,t[6]=_+m*c,t[10]=a*l}else if(e.order==="YXZ"){const f=l*d,m=l*p,_=c*d,M=c*p;t[0]=f+M*o,t[4]=_*o-m,t[8]=a*c,t[1]=a*p,t[5]=a*d,t[9]=-o,t[2]=m*o-_,t[6]=M+f*o,t[10]=a*l}else if(e.order==="ZXY"){const f=l*d,m=l*p,_=c*d,M=c*p;t[0]=f-M*o,t[4]=-a*p,t[8]=_+m*o,t[1]=m+_*o,t[5]=a*d,t[9]=M-f*o,t[2]=-a*c,t[6]=o,t[10]=a*l}else if(e.order==="ZYX"){const f=a*d,m=a*p,_=o*d,M=o*p;t[0]=l*d,t[4]=_*c-m,t[8]=f*c+M,t[1]=l*p,t[5]=M*c+f,t[9]=m*c-_,t[2]=-c,t[6]=o*l,t[10]=a*l}else if(e.order==="YZX"){const f=a*l,m=a*c,_=o*l,M=o*c;t[0]=l*d,t[4]=M-f*p,t[8]=_*p+m,t[1]=p,t[5]=a*d,t[9]=-o*d,t[2]=-c*d,t[6]=m*p+_,t[10]=f-M*p}else if(e.order==="XZY"){const f=a*l,m=a*c,_=o*l,M=o*c;t[0]=l*d,t[4]=-p,t[8]=c*d,t[1]=f*p+M,t[5]=a*d,t[9]=m*p-_,t[2]=_*p-m,t[6]=o*d,t[10]=M*p+f}return t[3]=0,t[7]=0,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromQuaternion(e){return this.compose(gc,e,vc)}lookAt(e,t,n){const r=this.elements;return Tt.subVectors(e,t),Tt.lengthSq()===0&&(Tt.z=1),Tt.normalize(),ln.crossVectors(n,Tt),ln.lengthSq()===0&&(Math.abs(n.z)===1?Tt.x+=1e-4:Tt.z+=1e-4,Tt.normalize(),ln.crossVectors(n,Tt)),ln.normalize(),Ii.crossVectors(Tt,ln),r[0]=ln.x,r[4]=Ii.x,r[8]=Tt.x,r[1]=ln.y,r[5]=Ii.y,r[9]=Tt.y,r[2]=ln.z,r[6]=Ii.z,r[10]=Tt.z,this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,r=t.elements,s=this.elements,a=n[0],o=n[4],l=n[8],c=n[12],d=n[1],p=n[5],f=n[9],m=n[13],_=n[2],M=n[6],h=n[10],u=n[14],y=n[3],S=n[7],T=n[11],N=n[15],R=r[0],w=r[4],I=r[8],K=r[12],g=r[1],E=r[5],k=r[9],G=r[13],X=r[2],J=r[6],H=r[10],Q=r[14],W=r[3],le=r[7],ce=r[11],_e=r[15];return s[0]=a*R+o*g+l*X+c*W,s[4]=a*w+o*E+l*J+c*le,s[8]=a*I+o*k+l*H+c*ce,s[12]=a*K+o*G+l*Q+c*_e,s[1]=d*R+p*g+f*X+m*W,s[5]=d*w+p*E+f*J+m*le,s[9]=d*I+p*k+f*H+m*ce,s[13]=d*K+p*G+f*Q+m*_e,s[2]=_*R+M*g+h*X+u*W,s[6]=_*w+M*E+h*J+u*le,s[10]=_*I+M*k+h*H+u*ce,s[14]=_*K+M*G+h*Q+u*_e,s[3]=y*R+S*g+T*X+N*W,s[7]=y*w+S*E+T*J+N*le,s[11]=y*I+S*k+T*H+N*ce,s[15]=y*K+S*G+T*Q+N*_e,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[4]*=e,t[8]*=e,t[12]*=e,t[1]*=e,t[5]*=e,t[9]*=e,t[13]*=e,t[2]*=e,t[6]*=e,t[10]*=e,t[14]*=e,t[3]*=e,t[7]*=e,t[11]*=e,t[15]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[4],r=e[8],s=e[12],a=e[1],o=e[5],l=e[9],c=e[13],d=e[2],p=e[6],f=e[10],m=e[14],_=e[3],M=e[7],h=e[11],u=e[15];return _*(+s*l*p-r*c*p-s*o*f+n*c*f+r*o*m-n*l*m)+M*(+t*l*m-t*c*f+s*a*f-r*a*m+r*c*d-s*l*d)+h*(+t*c*p-t*o*m-s*a*p+n*a*m+s*o*d-n*c*d)+u*(-r*o*d-t*l*p+t*o*f+r*a*p-n*a*f+n*l*d)}transpose(){const e=this.elements;let t;return t=e[1],e[1]=e[4],e[4]=t,t=e[2],e[2]=e[8],e[8]=t,t=e[6],e[6]=e[9],e[9]=t,t=e[3],e[3]=e[12],e[12]=t,t=e[7],e[7]=e[13],e[13]=t,t=e[11],e[11]=e[14],e[14]=t,this}setPosition(e,t,n){const r=this.elements;return e.isVector3?(r[12]=e.x,r[13]=e.y,r[14]=e.z):(r[12]=e,r[13]=t,r[14]=n),this}invert(){const e=this.elements,t=e[0],n=e[1],r=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],d=e[8],p=e[9],f=e[10],m=e[11],_=e[12],M=e[13],h=e[14],u=e[15],y=p*h*c-M*f*c+M*l*m-o*h*m-p*l*u+o*f*u,S=_*f*c-d*h*c-_*l*m+a*h*m+d*l*u-a*f*u,T=d*M*c-_*p*c+_*o*m-a*M*m-d*o*u+a*p*u,N=_*p*l-d*M*l-_*o*f+a*M*f+d*o*h-a*p*h,R=t*y+n*S+r*T+s*N;if(R===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const w=1/R;return e[0]=y*w,e[1]=(M*f*s-p*h*s-M*r*m+n*h*m+p*r*u-n*f*u)*w,e[2]=(o*h*s-M*l*s+M*r*c-n*h*c-o*r*u+n*l*u)*w,e[3]=(p*l*s-o*f*s-p*r*c+n*f*c+o*r*m-n*l*m)*w,e[4]=S*w,e[5]=(d*h*s-_*f*s+_*r*m-t*h*m-d*r*u+t*f*u)*w,e[6]=(_*l*s-a*h*s-_*r*c+t*h*c+a*r*u-t*l*u)*w,e[7]=(a*f*s-d*l*s+d*r*c-t*f*c-a*r*m+t*l*m)*w,e[8]=T*w,e[9]=(_*p*s-d*M*s-_*n*m+t*M*m+d*n*u-t*p*u)*w,e[10]=(a*M*s-_*o*s+_*n*c-t*M*c-a*n*u+t*o*u)*w,e[11]=(d*o*s-a*p*s-d*n*c+t*p*c+a*n*m-t*o*m)*w,e[12]=N*w,e[13]=(d*M*r-_*p*r+_*n*f-t*M*f-d*n*h+t*p*h)*w,e[14]=(_*o*r-a*M*r-_*n*l+t*M*l+a*n*h-t*o*h)*w,e[15]=(a*p*r-d*o*r+d*n*l-t*p*l-a*n*f+t*o*f)*w,this}scale(e){const t=this.elements,n=e.x,r=e.y,s=e.z;return t[0]*=n,t[4]*=r,t[8]*=s,t[1]*=n,t[5]*=r,t[9]*=s,t[2]*=n,t[6]*=r,t[10]*=s,t[3]*=n,t[7]*=r,t[11]*=s,this}getMaxScaleOnAxis(){const e=this.elements,t=e[0]*e[0]+e[1]*e[1]+e[2]*e[2],n=e[4]*e[4]+e[5]*e[5]+e[6]*e[6],r=e[8]*e[8]+e[9]*e[9]+e[10]*e[10];return Math.sqrt(Math.max(t,n,r))}makeTranslation(e,t,n){return e.isVector3?this.set(1,0,0,e.x,0,1,0,e.y,0,0,1,e.z,0,0,0,1):this.set(1,0,0,e,0,1,0,t,0,0,1,n,0,0,0,1),this}makeRotationX(e){const t=Math.cos(e),n=Math.sin(e);return this.set(1,0,0,0,0,t,-n,0,0,n,t,0,0,0,0,1),this}makeRotationY(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,0,n,0,0,1,0,0,-n,0,t,0,0,0,0,1),this}makeRotationZ(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,0,n,t,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(e,t){const n=Math.cos(t),r=Math.sin(t),s=1-n,a=e.x,o=e.y,l=e.z,c=s*a,d=s*o;return this.set(c*a+n,c*o-r*l,c*l+r*o,0,c*o+r*l,d*o+n,d*l-r*a,0,c*l-r*o,d*l+r*a,s*l*l+n,0,0,0,0,1),this}makeScale(e,t,n){return this.set(e,0,0,0,0,t,0,0,0,0,n,0,0,0,0,1),this}makeShear(e,t,n,r,s,a){return this.set(1,n,s,0,e,1,a,0,t,r,1,0,0,0,0,1),this}compose(e,t,n){const r=this.elements,s=t._x,a=t._y,o=t._z,l=t._w,c=s+s,d=a+a,p=o+o,f=s*c,m=s*d,_=s*p,M=a*d,h=a*p,u=o*p,y=l*c,S=l*d,T=l*p,N=n.x,R=n.y,w=n.z;return r[0]=(1-(M+u))*N,r[1]=(m+T)*N,r[2]=(_-S)*N,r[3]=0,r[4]=(m-T)*R,r[5]=(1-(f+u))*R,r[6]=(h+y)*R,r[7]=0,r[8]=(_+S)*w,r[9]=(h-y)*w,r[10]=(1-(f+M))*w,r[11]=0,r[12]=e.x,r[13]=e.y,r[14]=e.z,r[15]=1,this}decompose(e,t,n){const r=this.elements;let s=Bn.set(r[0],r[1],r[2]).length();const a=Bn.set(r[4],r[5],r[6]).length(),o=Bn.set(r[8],r[9],r[10]).length();this.determinant()<0&&(s=-s),e.x=r[12],e.y=r[13],e.z=r[14],Ft.copy(this);const c=1/s,d=1/a,p=1/o;return Ft.elements[0]*=c,Ft.elements[1]*=c,Ft.elements[2]*=c,Ft.elements[4]*=d,Ft.elements[5]*=d,Ft.elements[6]*=d,Ft.elements[8]*=p,Ft.elements[9]*=p,Ft.elements[10]*=p,t.setFromRotationMatrix(Ft),n.x=s,n.y=a,n.z=o,this}makePerspective(e,t,n,r,s,a,o=tn){const l=this.elements,c=2*s/(t-e),d=2*s/(n-r),p=(t+e)/(t-e),f=(n+r)/(n-r);let m,_;if(o===tn)m=-(a+s)/(a-s),_=-2*a*s/(a-s);else if(o===or)m=-a/(a-s),_=-a*s/(a-s);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+o);return l[0]=c,l[4]=0,l[8]=p,l[12]=0,l[1]=0,l[5]=d,l[9]=f,l[13]=0,l[2]=0,l[6]=0,l[10]=m,l[14]=_,l[3]=0,l[7]=0,l[11]=-1,l[15]=0,this}makeOrthographic(e,t,n,r,s,a,o=tn){const l=this.elements,c=1/(t-e),d=1/(n-r),p=1/(a-s),f=(t+e)*c,m=(n+r)*d;let _,M;if(o===tn)_=(a+s)*p,M=-2*p;else if(o===or)_=s*p,M=-1*p;else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+o);return l[0]=2*c,l[4]=0,l[8]=0,l[12]=-f,l[1]=0,l[5]=2*d,l[9]=0,l[13]=-m,l[2]=0,l[6]=0,l[10]=M,l[14]=-_,l[3]=0,l[7]=0,l[11]=0,l[15]=1,this}equals(e){const t=this.elements,n=e.elements;for(let r=0;r<16;r++)if(t[r]!==n[r])return!1;return!0}fromArray(e,t=0){for(let n=0;n<16;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e[t+9]=n[9],e[t+10]=n[10],e[t+11]=n[11],e[t+12]=n[12],e[t+13]=n[13],e[t+14]=n[14],e[t+15]=n[15],e}}const Bn=new D,Ft=new et,gc=new D(0,0,0),vc=new D(1,1,1),ln=new D,Ii=new D,Tt=new D,ba=new et,wa=new Si;class Xt{constructor(e=0,t=0,n=0,r=Xt.DEFAULT_ORDER){this.isEuler=!0,this._x=e,this._y=t,this._z=n,this._order=r}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get order(){return this._order}set order(e){this._order=e,this._onChangeCallback()}set(e,t,n,r=this._order){return this._x=e,this._y=t,this._z=n,this._order=r,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(e){return this._x=e._x,this._y=e._y,this._z=e._z,this._order=e._order,this._onChangeCallback(),this}setFromRotationMatrix(e,t=this._order,n=!0){const r=e.elements,s=r[0],a=r[4],o=r[8],l=r[1],c=r[5],d=r[9],p=r[2],f=r[6],m=r[10];switch(t){case"XYZ":this._y=Math.asin(xt(o,-1,1)),Math.abs(o)<.9999999?(this._x=Math.atan2(-d,m),this._z=Math.atan2(-a,s)):(this._x=Math.atan2(f,c),this._z=0);break;case"YXZ":this._x=Math.asin(-xt(d,-1,1)),Math.abs(d)<.9999999?(this._y=Math.atan2(o,m),this._z=Math.atan2(l,c)):(this._y=Math.atan2(-p,s),this._z=0);break;case"ZXY":this._x=Math.asin(xt(f,-1,1)),Math.abs(f)<.9999999?(this._y=Math.atan2(-p,m),this._z=Math.atan2(-a,c)):(this._y=0,this._z=Math.atan2(l,s));break;case"ZYX":this._y=Math.asin(-xt(p,-1,1)),Math.abs(p)<.9999999?(this._x=Math.atan2(f,m),this._z=Math.atan2(l,s)):(this._x=0,this._z=Math.atan2(-a,c));break;case"YZX":this._z=Math.asin(xt(l,-1,1)),Math.abs(l)<.9999999?(this._x=Math.atan2(-d,c),this._y=Math.atan2(-p,s)):(this._x=0,this._y=Math.atan2(o,m));break;case"XZY":this._z=Math.asin(-xt(a,-1,1)),Math.abs(a)<.9999999?(this._x=Math.atan2(f,c),this._y=Math.atan2(o,s)):(this._x=Math.atan2(-d,m),this._y=0);break;default:console.warn("THREE.Euler: .setFromRotationMatrix() encountered an unknown order: "+t)}return this._order=t,n===!0&&this._onChangeCallback(),this}setFromQuaternion(e,t,n){return ba.makeRotationFromQuaternion(e),this.setFromRotationMatrix(ba,t,n)}setFromVector3(e,t=this._order){return this.set(e.x,e.y,e.z,t)}reorder(e){return wa.setFromEuler(this),this.setFromQuaternion(wa,e)}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._order===this._order}fromArray(e){return this._x=e[0],this._y=e[1],this._z=e[2],e[3]!==void 0&&(this._order=e[3]),this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._order,e}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}Xt.DEFAULT_ORDER="XYZ";class Oo{constructor(){this.mask=1}set(e){this.mask=(1<<e|0)>>>0}enable(e){this.mask|=1<<e|0}enableAll(){this.mask=-1}toggle(e){this.mask^=1<<e|0}disable(e){this.mask&=~(1<<e|0)}disableAll(){this.mask=0}test(e){return(this.mask&e.mask)!==0}isEnabled(e){return(this.mask&(1<<e|0))!==0}}let xc=0;const Ra=new D,zn=new Si,$t=new et,Ni=new D,di=new D,Mc=new D,Sc=new Si,Ca=new D(1,0,0),Pa=new D(0,1,0),La=new D(0,0,1),Da={type:"added"},Ec={type:"removed"},Hn={type:"childadded",child:null},Fr={type:"childremoved",child:null};class ht extends ii{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:xc++}),this.uuid=Mi(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=ht.DEFAULT_UP.clone();const e=new D,t=new Xt,n=new Si,r=new D(1,1,1);function s(){n.setFromEuler(t,!1)}function a(){t.setFromQuaternion(n,void 0,!1)}t._onChange(s),n._onChange(a),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:e},rotation:{configurable:!0,enumerable:!0,value:t},quaternion:{configurable:!0,enumerable:!0,value:n},scale:{configurable:!0,enumerable:!0,value:r},modelViewMatrix:{value:new et},normalMatrix:{value:new De}}),this.matrix=new et,this.matrixWorld=new et,this.matrixAutoUpdate=ht.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=ht.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new Oo,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.userData={}}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(e){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(e),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(e){return this.quaternion.premultiply(e),this}setRotationFromAxisAngle(e,t){this.quaternion.setFromAxisAngle(e,t)}setRotationFromEuler(e){this.quaternion.setFromEuler(e,!0)}setRotationFromMatrix(e){this.quaternion.setFromRotationMatrix(e)}setRotationFromQuaternion(e){this.quaternion.copy(e)}rotateOnAxis(e,t){return zn.setFromAxisAngle(e,t),this.quaternion.multiply(zn),this}rotateOnWorldAxis(e,t){return zn.setFromAxisAngle(e,t),this.quaternion.premultiply(zn),this}rotateX(e){return this.rotateOnAxis(Ca,e)}rotateY(e){return this.rotateOnAxis(Pa,e)}rotateZ(e){return this.rotateOnAxis(La,e)}translateOnAxis(e,t){return Ra.copy(e).applyQuaternion(this.quaternion),this.position.add(Ra.multiplyScalar(t)),this}translateX(e){return this.translateOnAxis(Ca,e)}translateY(e){return this.translateOnAxis(Pa,e)}translateZ(e){return this.translateOnAxis(La,e)}localToWorld(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4(this.matrixWorld)}worldToLocal(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4($t.copy(this.matrixWorld).invert())}lookAt(e,t,n){e.isVector3?Ni.copy(e):Ni.set(e,t,n);const r=this.parent;this.updateWorldMatrix(!0,!1),di.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?$t.lookAt(di,Ni,this.up):$t.lookAt(Ni,di,this.up),this.quaternion.setFromRotationMatrix($t),r&&($t.extractRotation(r.matrixWorld),zn.setFromRotationMatrix($t),this.quaternion.premultiply(zn.invert()))}add(e){if(arguments.length>1){for(let t=0;t<arguments.length;t++)this.add(arguments[t]);return this}return e===this?(console.error("THREE.Object3D.add: object can't be added as a child of itself.",e),this):(e&&e.isObject3D?(e.removeFromParent(),e.parent=this,this.children.push(e),e.dispatchEvent(Da),Hn.child=e,this.dispatchEvent(Hn),Hn.child=null):console.error("THREE.Object3D.add: object not an instance of THREE.Object3D.",e),this)}remove(e){if(arguments.length>1){for(let n=0;n<arguments.length;n++)this.remove(arguments[n]);return this}const t=this.children.indexOf(e);return t!==-1&&(e.parent=null,this.children.splice(t,1),e.dispatchEvent(Ec),Fr.child=e,this.dispatchEvent(Fr),Fr.child=null),this}removeFromParent(){const e=this.parent;return e!==null&&e.remove(this),this}clear(){return this.remove(...this.children)}attach(e){return this.updateWorldMatrix(!0,!1),$t.copy(this.matrixWorld).invert(),e.parent!==null&&(e.parent.updateWorldMatrix(!0,!1),$t.multiply(e.parent.matrixWorld)),e.applyMatrix4($t),e.removeFromParent(),e.parent=this,this.children.push(e),e.updateWorldMatrix(!1,!0),e.dispatchEvent(Da),Hn.child=e,this.dispatchEvent(Hn),Hn.child=null,this}getObjectById(e){return this.getObjectByProperty("id",e)}getObjectByName(e){return this.getObjectByProperty("name",e)}getObjectByProperty(e,t){if(this[e]===t)return this;for(let n=0,r=this.children.length;n<r;n++){const a=this.children[n].getObjectByProperty(e,t);if(a!==void 0)return a}}getObjectsByProperty(e,t,n=[]){this[e]===t&&n.push(this);const r=this.children;for(let s=0,a=r.length;s<a;s++)r[s].getObjectsByProperty(e,t,n);return n}getWorldPosition(e){return this.updateWorldMatrix(!0,!1),e.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(di,e,Mc),e}getWorldScale(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(di,Sc,e),e}getWorldDirection(e){this.updateWorldMatrix(!0,!1);const t=this.matrixWorld.elements;return e.set(t[8],t[9],t[10]).normalize()}raycast(){}traverse(e){e(this);const t=this.children;for(let n=0,r=t.length;n<r;n++)t[n].traverse(e)}traverseVisible(e){if(this.visible===!1)return;e(this);const t=this.children;for(let n=0,r=t.length;n<r;n++)t[n].traverseVisible(e)}traverseAncestors(e){const t=this.parent;t!==null&&(e(t),t.traverseAncestors(e))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale),this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(e){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||e)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,e=!0);const t=this.children;for(let n=0,r=t.length;n<r;n++)t[n].updateMatrixWorld(e)}updateWorldMatrix(e,t){const n=this.parent;if(e===!0&&n!==null&&n.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),t===!0){const r=this.children;for(let s=0,a=r.length;s<a;s++)r[s].updateWorldMatrix(!1,!0)}}toJSON(e){const t=e===void 0||typeof e=="string",n={};t&&(e={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},n.metadata={version:4.6,type:"Object",generator:"Object3D.toJSON"});const r={};r.uuid=this.uuid,r.type=this.type,this.name!==""&&(r.name=this.name),this.castShadow===!0&&(r.castShadow=!0),this.receiveShadow===!0&&(r.receiveShadow=!0),this.visible===!1&&(r.visible=!1),this.frustumCulled===!1&&(r.frustumCulled=!1),this.renderOrder!==0&&(r.renderOrder=this.renderOrder),Object.keys(this.userData).length>0&&(r.userData=this.userData),r.layers=this.layers.mask,r.matrix=this.matrix.toArray(),r.up=this.up.toArray(),this.matrixAutoUpdate===!1&&(r.matrixAutoUpdate=!1),this.isInstancedMesh&&(r.type="InstancedMesh",r.count=this.count,r.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(r.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(r.type="BatchedMesh",r.perObjectFrustumCulled=this.perObjectFrustumCulled,r.sortObjects=this.sortObjects,r.drawRanges=this._drawRanges,r.reservedRanges=this._reservedRanges,r.visibility=this._visibility,r.active=this._active,r.bounds=this._bounds.map(o=>({boxInitialized:o.boxInitialized,boxMin:o.box.min.toArray(),boxMax:o.box.max.toArray(),sphereInitialized:o.sphereInitialized,sphereRadius:o.sphere.radius,sphereCenter:o.sphere.center.toArray()})),r.maxInstanceCount=this._maxInstanceCount,r.maxVertexCount=this._maxVertexCount,r.maxIndexCount=this._maxIndexCount,r.geometryInitialized=this._geometryInitialized,r.geometryCount=this._geometryCount,r.matricesTexture=this._matricesTexture.toJSON(e),this._colorsTexture!==null&&(r.colorsTexture=this._colorsTexture.toJSON(e)),this.boundingSphere!==null&&(r.boundingSphere={center:r.boundingSphere.center.toArray(),radius:r.boundingSphere.radius}),this.boundingBox!==null&&(r.boundingBox={min:r.boundingBox.min.toArray(),max:r.boundingBox.max.toArray()}));function s(o,l){return o[l.uuid]===void 0&&(o[l.uuid]=l.toJSON(e)),l.uuid}if(this.isScene)this.background&&(this.background.isColor?r.background=this.background.toJSON():this.background.isTexture&&(r.background=this.background.toJSON(e).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(r.environment=this.environment.toJSON(e).uuid);else if(this.isMesh||this.isLine||this.isPoints){r.geometry=s(e.geometries,this.geometry);const o=this.geometry.parameters;if(o!==void 0&&o.shapes!==void 0){const l=o.shapes;if(Array.isArray(l))for(let c=0,d=l.length;c<d;c++){const p=l[c];s(e.shapes,p)}else s(e.shapes,l)}}if(this.isSkinnedMesh&&(r.bindMode=this.bindMode,r.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(s(e.skeletons,this.skeleton),r.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const o=[];for(let l=0,c=this.material.length;l<c;l++)o.push(s(e.materials,this.material[l]));r.material=o}else r.material=s(e.materials,this.material);if(this.children.length>0){r.children=[];for(let o=0;o<this.children.length;o++)r.children.push(this.children[o].toJSON(e).object)}if(this.animations.length>0){r.animations=[];for(let o=0;o<this.animations.length;o++){const l=this.animations[o];r.animations.push(s(e.animations,l))}}if(t){const o=a(e.geometries),l=a(e.materials),c=a(e.textures),d=a(e.images),p=a(e.shapes),f=a(e.skeletons),m=a(e.animations),_=a(e.nodes);o.length>0&&(n.geometries=o),l.length>0&&(n.materials=l),c.length>0&&(n.textures=c),d.length>0&&(n.images=d),p.length>0&&(n.shapes=p),f.length>0&&(n.skeletons=f),m.length>0&&(n.animations=m),_.length>0&&(n.nodes=_)}return n.object=r,n;function a(o){const l=[];for(const c in o){const d=o[c];delete d.metadata,l.push(d)}return l}}clone(e){return new this.constructor().copy(this,e)}copy(e,t=!0){if(this.name=e.name,this.up.copy(e.up),this.position.copy(e.position),this.rotation.order=e.rotation.order,this.quaternion.copy(e.quaternion),this.scale.copy(e.scale),this.matrix.copy(e.matrix),this.matrixWorld.copy(e.matrixWorld),this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrixWorldAutoUpdate=e.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=e.matrixWorldNeedsUpdate,this.layers.mask=e.layers.mask,this.visible=e.visible,this.castShadow=e.castShadow,this.receiveShadow=e.receiveShadow,this.frustumCulled=e.frustumCulled,this.renderOrder=e.renderOrder,this.animations=e.animations.slice(),this.userData=JSON.parse(JSON.stringify(e.userData)),t===!0)for(let n=0;n<e.children.length;n++){const r=e.children[n];this.add(r.clone())}return this}}ht.DEFAULT_UP=new D(0,1,0);ht.DEFAULT_MATRIX_AUTO_UPDATE=!0;ht.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;const Ot=new D,Zt=new D,Or=new D,jt=new D,Gn=new D,Vn=new D,Ua=new D,Br=new D,zr=new D,Hr=new D,Gr=new Ze,Vr=new Ze,kr=new Ze;class zt{constructor(e=new D,t=new D,n=new D){this.a=e,this.b=t,this.c=n}static getNormal(e,t,n,r){r.subVectors(n,t),Ot.subVectors(e,t),r.cross(Ot);const s=r.lengthSq();return s>0?r.multiplyScalar(1/Math.sqrt(s)):r.set(0,0,0)}static getBarycoord(e,t,n,r,s){Ot.subVectors(r,t),Zt.subVectors(n,t),Or.subVectors(e,t);const a=Ot.dot(Ot),o=Ot.dot(Zt),l=Ot.dot(Or),c=Zt.dot(Zt),d=Zt.dot(Or),p=a*c-o*o;if(p===0)return s.set(0,0,0),null;const f=1/p,m=(c*l-o*d)*f,_=(a*d-o*l)*f;return s.set(1-m-_,_,m)}static containsPoint(e,t,n,r){return this.getBarycoord(e,t,n,r,jt)===null?!1:jt.x>=0&&jt.y>=0&&jt.x+jt.y<=1}static getInterpolation(e,t,n,r,s,a,o,l){return this.getBarycoord(e,t,n,r,jt)===null?(l.x=0,l.y=0,"z"in l&&(l.z=0),"w"in l&&(l.w=0),null):(l.setScalar(0),l.addScaledVector(s,jt.x),l.addScaledVector(a,jt.y),l.addScaledVector(o,jt.z),l)}static getInterpolatedAttribute(e,t,n,r,s,a){return Gr.setScalar(0),Vr.setScalar(0),kr.setScalar(0),Gr.fromBufferAttribute(e,t),Vr.fromBufferAttribute(e,n),kr.fromBufferAttribute(e,r),a.setScalar(0),a.addScaledVector(Gr,s.x),a.addScaledVector(Vr,s.y),a.addScaledVector(kr,s.z),a}static isFrontFacing(e,t,n,r){return Ot.subVectors(n,t),Zt.subVectors(e,t),Ot.cross(Zt).dot(r)<0}set(e,t,n){return this.a.copy(e),this.b.copy(t),this.c.copy(n),this}setFromPointsAndIndices(e,t,n,r){return this.a.copy(e[t]),this.b.copy(e[n]),this.c.copy(e[r]),this}setFromAttributeAndIndices(e,t,n,r){return this.a.fromBufferAttribute(e,t),this.b.fromBufferAttribute(e,n),this.c.fromBufferAttribute(e,r),this}clone(){return new this.constructor().copy(this)}copy(e){return this.a.copy(e.a),this.b.copy(e.b),this.c.copy(e.c),this}getArea(){return Ot.subVectors(this.c,this.b),Zt.subVectors(this.a,this.b),Ot.cross(Zt).length()*.5}getMidpoint(e){return e.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(e){return zt.getNormal(this.a,this.b,this.c,e)}getPlane(e){return e.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(e,t){return zt.getBarycoord(e,this.a,this.b,this.c,t)}getInterpolation(e,t,n,r,s){return zt.getInterpolation(e,this.a,this.b,this.c,t,n,r,s)}containsPoint(e){return zt.containsPoint(e,this.a,this.b,this.c)}isFrontFacing(e){return zt.isFrontFacing(this.a,this.b,this.c,e)}intersectsBox(e){return e.intersectsTriangle(this)}closestPointToPoint(e,t){const n=this.a,r=this.b,s=this.c;let a,o;Gn.subVectors(r,n),Vn.subVectors(s,n),Br.subVectors(e,n);const l=Gn.dot(Br),c=Vn.dot(Br);if(l<=0&&c<=0)return t.copy(n);zr.subVectors(e,r);const d=Gn.dot(zr),p=Vn.dot(zr);if(d>=0&&p<=d)return t.copy(r);const f=l*p-d*c;if(f<=0&&l>=0&&d<=0)return a=l/(l-d),t.copy(n).addScaledVector(Gn,a);Hr.subVectors(e,s);const m=Gn.dot(Hr),_=Vn.dot(Hr);if(_>=0&&m<=_)return t.copy(s);const M=m*c-l*_;if(M<=0&&c>=0&&_<=0)return o=c/(c-_),t.copy(n).addScaledVector(Vn,o);const h=d*_-m*p;if(h<=0&&p-d>=0&&m-_>=0)return Ua.subVectors(s,r),o=(p-d)/(p-d+(m-_)),t.copy(r).addScaledVector(Ua,o);const u=1/(h+M+f);return a=M*u,o=f*u,t.copy(n).addScaledVector(Gn,a).addScaledVector(Vn,o)}equals(e){return e.a.equals(this.a)&&e.b.equals(this.b)&&e.c.equals(this.c)}}const Bo={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},cn={h:0,s:0,l:0},Fi={h:0,s:0,l:0};function Wr(i,e,t){return t<0&&(t+=1),t>1&&(t-=1),t<1/6?i+(e-i)*6*t:t<1/2?e:t<2/3?i+(e-i)*6*(2/3-t):i}class Oe{constructor(e,t,n){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(e,t,n)}set(e,t,n){if(t===void 0&&n===void 0){const r=e;r&&r.isColor?this.copy(r):typeof r=="number"?this.setHex(r):typeof r=="string"&&this.setStyle(r)}else this.setRGB(e,t,n);return this}setScalar(e){return this.r=e,this.g=e,this.b=e,this}setHex(e,t=Vt){return e=Math.floor(e),this.r=(e>>16&255)/255,this.g=(e>>8&255)/255,this.b=(e&255)/255,Ke.toWorkingColorSpace(this,t),this}setRGB(e,t,n,r=Ke.workingColorSpace){return this.r=e,this.g=t,this.b=n,Ke.toWorkingColorSpace(this,r),this}setHSL(e,t,n,r=Ke.workingColorSpace){if(e=sc(e,1),t=xt(t,0,1),n=xt(n,0,1),t===0)this.r=this.g=this.b=n;else{const s=n<=.5?n*(1+t):n+t-n*t,a=2*n-s;this.r=Wr(a,s,e+1/3),this.g=Wr(a,s,e),this.b=Wr(a,s,e-1/3)}return Ke.toWorkingColorSpace(this,r),this}setStyle(e,t=Vt){function n(s){s!==void 0&&parseFloat(s)<1&&console.warn("THREE.Color: Alpha component of "+e+" will be ignored.")}let r;if(r=/^(\w+)\(([^\)]*)\)/.exec(e)){let s;const a=r[1],o=r[2];switch(a){case"rgb":case"rgba":if(s=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(255,parseInt(s[1],10))/255,Math.min(255,parseInt(s[2],10))/255,Math.min(255,parseInt(s[3],10))/255,t);if(s=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(100,parseInt(s[1],10))/100,Math.min(100,parseInt(s[2],10))/100,Math.min(100,parseInt(s[3],10))/100,t);break;case"hsl":case"hsla":if(s=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setHSL(parseFloat(s[1])/360,parseFloat(s[2])/100,parseFloat(s[3])/100,t);break;default:console.warn("THREE.Color: Unknown color model "+e)}}else if(r=/^\#([A-Fa-f\d]+)$/.exec(e)){const s=r[1],a=s.length;if(a===3)return this.setRGB(parseInt(s.charAt(0),16)/15,parseInt(s.charAt(1),16)/15,parseInt(s.charAt(2),16)/15,t);if(a===6)return this.setHex(parseInt(s,16),t);console.warn("THREE.Color: Invalid hex color "+e)}else if(e&&e.length>0)return this.setColorName(e,t);return this}setColorName(e,t=Vt){const n=Bo[e.toLowerCase()];return n!==void 0?this.setHex(n,t):console.warn("THREE.Color: Unknown color "+e),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(e){return this.r=e.r,this.g=e.g,this.b=e.b,this}copySRGBToLinear(e){return this.r=Zn(e.r),this.g=Zn(e.g),this.b=Zn(e.b),this}copyLinearToSRGB(e){return this.r=Rr(e.r),this.g=Rr(e.g),this.b=Rr(e.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(e=Vt){return Ke.fromWorkingColorSpace(ft.copy(this),e),Math.round(xt(ft.r*255,0,255))*65536+Math.round(xt(ft.g*255,0,255))*256+Math.round(xt(ft.b*255,0,255))}getHexString(e=Vt){return("000000"+this.getHex(e).toString(16)).slice(-6)}getHSL(e,t=Ke.workingColorSpace){Ke.fromWorkingColorSpace(ft.copy(this),t);const n=ft.r,r=ft.g,s=ft.b,a=Math.max(n,r,s),o=Math.min(n,r,s);let l,c;const d=(o+a)/2;if(o===a)l=0,c=0;else{const p=a-o;switch(c=d<=.5?p/(a+o):p/(2-a-o),a){case n:l=(r-s)/p+(r<s?6:0);break;case r:l=(s-n)/p+2;break;case s:l=(n-r)/p+4;break}l/=6}return e.h=l,e.s=c,e.l=d,e}getRGB(e,t=Ke.workingColorSpace){return Ke.fromWorkingColorSpace(ft.copy(this),t),e.r=ft.r,e.g=ft.g,e.b=ft.b,e}getStyle(e=Vt){Ke.fromWorkingColorSpace(ft.copy(this),e);const t=ft.r,n=ft.g,r=ft.b;return e!==Vt?`color(${e} ${t.toFixed(3)} ${n.toFixed(3)} ${r.toFixed(3)})`:`rgb(${Math.round(t*255)},${Math.round(n*255)},${Math.round(r*255)})`}offsetHSL(e,t,n){return this.getHSL(cn),this.setHSL(cn.h+e,cn.s+t,cn.l+n)}add(e){return this.r+=e.r,this.g+=e.g,this.b+=e.b,this}addColors(e,t){return this.r=e.r+t.r,this.g=e.g+t.g,this.b=e.b+t.b,this}addScalar(e){return this.r+=e,this.g+=e,this.b+=e,this}sub(e){return this.r=Math.max(0,this.r-e.r),this.g=Math.max(0,this.g-e.g),this.b=Math.max(0,this.b-e.b),this}multiply(e){return this.r*=e.r,this.g*=e.g,this.b*=e.b,this}multiplyScalar(e){return this.r*=e,this.g*=e,this.b*=e,this}lerp(e,t){return this.r+=(e.r-this.r)*t,this.g+=(e.g-this.g)*t,this.b+=(e.b-this.b)*t,this}lerpColors(e,t,n){return this.r=e.r+(t.r-e.r)*n,this.g=e.g+(t.g-e.g)*n,this.b=e.b+(t.b-e.b)*n,this}lerpHSL(e,t){this.getHSL(cn),e.getHSL(Fi);const n=br(cn.h,Fi.h,t),r=br(cn.s,Fi.s,t),s=br(cn.l,Fi.l,t);return this.setHSL(n,r,s),this}setFromVector3(e){return this.r=e.x,this.g=e.y,this.b=e.z,this}applyMatrix3(e){const t=this.r,n=this.g,r=this.b,s=e.elements;return this.r=s[0]*t+s[3]*n+s[6]*r,this.g=s[1]*t+s[4]*n+s[7]*r,this.b=s[2]*t+s[5]*n+s[8]*r,this}equals(e){return e.r===this.r&&e.g===this.g&&e.b===this.b}fromArray(e,t=0){return this.r=e[t],this.g=e[t+1],this.b=e[t+2],this}toArray(e=[],t=0){return e[t]=this.r,e[t+1]=this.g,e[t+2]=this.b,e}fromBufferAttribute(e,t){return this.r=e.getX(t),this.g=e.getY(t),this.b=e.getZ(t),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const ft=new Oe;Oe.NAMES=Bo;let yc=0;class ri extends ii{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:yc++}),this.uuid=Mi(),this.name="",this.type="Material",this.blending=Kn,this.side=pn,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=rs,this.blendDst=ss,this.blendEquation=bn,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new Oe(0,0,0),this.blendAlpha=0,this.depthFunc=jn,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=xa,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Un,this.stencilZFail=Un,this.stencilZPass=Un,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(e){this._alphaTest>0!=e>0&&this.version++,this._alphaTest=e}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(e){if(e!==void 0)for(const t in e){const n=e[t];if(n===void 0){console.warn(`THREE.Material: parameter '${t}' has value of undefined.`);continue}const r=this[t];if(r===void 0){console.warn(`THREE.Material: '${t}' is not a property of THREE.${this.type}.`);continue}r&&r.isColor?r.set(n):r&&r.isVector3&&n&&n.isVector3?r.copy(n):this[t]=n}}toJSON(e){const t=e===void 0||typeof e=="string";t&&(e={textures:{},images:{}});const n={metadata:{version:4.6,type:"Material",generator:"Material.toJSON"}};n.uuid=this.uuid,n.type=this.type,this.name!==""&&(n.name=this.name),this.color&&this.color.isColor&&(n.color=this.color.getHex()),this.roughness!==void 0&&(n.roughness=this.roughness),this.metalness!==void 0&&(n.metalness=this.metalness),this.sheen!==void 0&&(n.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(n.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(n.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(n.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(n.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(n.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(n.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(n.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(n.shininess=this.shininess),this.clearcoat!==void 0&&(n.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(n.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(n.clearcoatMap=this.clearcoatMap.toJSON(e).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(n.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(e).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(n.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(e).uuid,n.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.dispersion!==void 0&&(n.dispersion=this.dispersion),this.iridescence!==void 0&&(n.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(n.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(n.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(n.iridescenceMap=this.iridescenceMap.toJSON(e).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(n.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(e).uuid),this.anisotropy!==void 0&&(n.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(n.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(n.anisotropyMap=this.anisotropyMap.toJSON(e).uuid),this.map&&this.map.isTexture&&(n.map=this.map.toJSON(e).uuid),this.matcap&&this.matcap.isTexture&&(n.matcap=this.matcap.toJSON(e).uuid),this.alphaMap&&this.alphaMap.isTexture&&(n.alphaMap=this.alphaMap.toJSON(e).uuid),this.lightMap&&this.lightMap.isTexture&&(n.lightMap=this.lightMap.toJSON(e).uuid,n.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(n.aoMap=this.aoMap.toJSON(e).uuid,n.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(n.bumpMap=this.bumpMap.toJSON(e).uuid,n.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(n.normalMap=this.normalMap.toJSON(e).uuid,n.normalMapType=this.normalMapType,n.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(n.displacementMap=this.displacementMap.toJSON(e).uuid,n.displacementScale=this.displacementScale,n.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(n.roughnessMap=this.roughnessMap.toJSON(e).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(n.metalnessMap=this.metalnessMap.toJSON(e).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(n.emissiveMap=this.emissiveMap.toJSON(e).uuid),this.specularMap&&this.specularMap.isTexture&&(n.specularMap=this.specularMap.toJSON(e).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(n.specularIntensityMap=this.specularIntensityMap.toJSON(e).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(n.specularColorMap=this.specularColorMap.toJSON(e).uuid),this.envMap&&this.envMap.isTexture&&(n.envMap=this.envMap.toJSON(e).uuid,this.combine!==void 0&&(n.combine=this.combine)),this.envMapRotation!==void 0&&(n.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(n.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(n.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(n.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(n.gradientMap=this.gradientMap.toJSON(e).uuid),this.transmission!==void 0&&(n.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(n.transmissionMap=this.transmissionMap.toJSON(e).uuid),this.thickness!==void 0&&(n.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(n.thicknessMap=this.thicknessMap.toJSON(e).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(n.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(n.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(n.size=this.size),this.shadowSide!==null&&(n.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(n.sizeAttenuation=this.sizeAttenuation),this.blending!==Kn&&(n.blending=this.blending),this.side!==pn&&(n.side=this.side),this.vertexColors===!0&&(n.vertexColors=!0),this.opacity<1&&(n.opacity=this.opacity),this.transparent===!0&&(n.transparent=!0),this.blendSrc!==rs&&(n.blendSrc=this.blendSrc),this.blendDst!==ss&&(n.blendDst=this.blendDst),this.blendEquation!==bn&&(n.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(n.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(n.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(n.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(n.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(n.blendAlpha=this.blendAlpha),this.depthFunc!==jn&&(n.depthFunc=this.depthFunc),this.depthTest===!1&&(n.depthTest=this.depthTest),this.depthWrite===!1&&(n.depthWrite=this.depthWrite),this.colorWrite===!1&&(n.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(n.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==xa&&(n.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(n.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(n.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Un&&(n.stencilFail=this.stencilFail),this.stencilZFail!==Un&&(n.stencilZFail=this.stencilZFail),this.stencilZPass!==Un&&(n.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(n.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(n.rotation=this.rotation),this.polygonOffset===!0&&(n.polygonOffset=!0),this.polygonOffsetFactor!==0&&(n.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(n.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(n.linewidth=this.linewidth),this.dashSize!==void 0&&(n.dashSize=this.dashSize),this.gapSize!==void 0&&(n.gapSize=this.gapSize),this.scale!==void 0&&(n.scale=this.scale),this.dithering===!0&&(n.dithering=!0),this.alphaTest>0&&(n.alphaTest=this.alphaTest),this.alphaHash===!0&&(n.alphaHash=!0),this.alphaToCoverage===!0&&(n.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(n.premultipliedAlpha=!0),this.forceSinglePass===!0&&(n.forceSinglePass=!0),this.wireframe===!0&&(n.wireframe=!0),this.wireframeLinewidth>1&&(n.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(n.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(n.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(n.flatShading=!0),this.visible===!1&&(n.visible=!1),this.toneMapped===!1&&(n.toneMapped=!1),this.fog===!1&&(n.fog=!1),Object.keys(this.userData).length>0&&(n.userData=this.userData);function r(s){const a=[];for(const o in s){const l=s[o];delete l.metadata,a.push(l)}return a}if(t){const s=r(e.textures),a=r(e.images);s.length>0&&(n.textures=s),a.length>0&&(n.images=a)}return n}clone(){return new this.constructor().copy(this)}copy(e){this.name=e.name,this.blending=e.blending,this.side=e.side,this.vertexColors=e.vertexColors,this.opacity=e.opacity,this.transparent=e.transparent,this.blendSrc=e.blendSrc,this.blendDst=e.blendDst,this.blendEquation=e.blendEquation,this.blendSrcAlpha=e.blendSrcAlpha,this.blendDstAlpha=e.blendDstAlpha,this.blendEquationAlpha=e.blendEquationAlpha,this.blendColor.copy(e.blendColor),this.blendAlpha=e.blendAlpha,this.depthFunc=e.depthFunc,this.depthTest=e.depthTest,this.depthWrite=e.depthWrite,this.stencilWriteMask=e.stencilWriteMask,this.stencilFunc=e.stencilFunc,this.stencilRef=e.stencilRef,this.stencilFuncMask=e.stencilFuncMask,this.stencilFail=e.stencilFail,this.stencilZFail=e.stencilZFail,this.stencilZPass=e.stencilZPass,this.stencilWrite=e.stencilWrite;const t=e.clippingPlanes;let n=null;if(t!==null){const r=t.length;n=new Array(r);for(let s=0;s!==r;++s)n[s]=t[s].clone()}return this.clippingPlanes=n,this.clipIntersection=e.clipIntersection,this.clipShadows=e.clipShadows,this.shadowSide=e.shadowSide,this.colorWrite=e.colorWrite,this.precision=e.precision,this.polygonOffset=e.polygonOffset,this.polygonOffsetFactor=e.polygonOffsetFactor,this.polygonOffsetUnits=e.polygonOffsetUnits,this.dithering=e.dithering,this.alphaTest=e.alphaTest,this.alphaHash=e.alphaHash,this.alphaToCoverage=e.alphaToCoverage,this.premultipliedAlpha=e.premultipliedAlpha,this.forceSinglePass=e.forceSinglePass,this.visible=e.visible,this.toneMapped=e.toneMapped,this.userData=JSON.parse(JSON.stringify(e.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(e){e===!0&&this.version++}onBuild(){console.warn("Material: onBuild() has been removed.")}}class cr extends ri{constructor(e){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new Oe(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new Xt,this.combine=xo,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.specularMap=e.specularMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.combine=e.combine,this.reflectivity=e.reflectivity,this.refractionRatio=e.refractionRatio,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.fog=e.fog,this}}const st=new D,Oi=new ze;class Wt{constructor(e,t,n=!1){if(Array.isArray(e))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,this.name="",this.array=e,this.itemSize=t,this.count=e!==void 0?e.length/t:0,this.normalized=n,this.usage=Ma,this.updateRanges=[],this.gpuType=en,this.version=0}onUploadCallback(){}set needsUpdate(e){e===!0&&this.version++}setUsage(e){return this.usage=e,this}addUpdateRange(e,t){this.updateRanges.push({start:e,count:t})}clearUpdateRanges(){this.updateRanges.length=0}copy(e){return this.name=e.name,this.array=new e.array.constructor(e.array),this.itemSize=e.itemSize,this.count=e.count,this.normalized=e.normalized,this.usage=e.usage,this.gpuType=e.gpuType,this}copyAt(e,t,n){e*=this.itemSize,n*=t.itemSize;for(let r=0,s=this.itemSize;r<s;r++)this.array[e+r]=t.array[n+r];return this}copyArray(e){return this.array.set(e),this}applyMatrix3(e){if(this.itemSize===2)for(let t=0,n=this.count;t<n;t++)Oi.fromBufferAttribute(this,t),Oi.applyMatrix3(e),this.setXY(t,Oi.x,Oi.y);else if(this.itemSize===3)for(let t=0,n=this.count;t<n;t++)st.fromBufferAttribute(this,t),st.applyMatrix3(e),this.setXYZ(t,st.x,st.y,st.z);return this}applyMatrix4(e){for(let t=0,n=this.count;t<n;t++)st.fromBufferAttribute(this,t),st.applyMatrix4(e),this.setXYZ(t,st.x,st.y,st.z);return this}applyNormalMatrix(e){for(let t=0,n=this.count;t<n;t++)st.fromBufferAttribute(this,t),st.applyNormalMatrix(e),this.setXYZ(t,st.x,st.y,st.z);return this}transformDirection(e){for(let t=0,n=this.count;t<n;t++)st.fromBufferAttribute(this,t),st.transformDirection(e),this.setXYZ(t,st.x,st.y,st.z);return this}set(e,t=0){return this.array.set(e,t),this}getComponent(e,t){let n=this.array[e*this.itemSize+t];return this.normalized&&(n=li(n,this.array)),n}setComponent(e,t,n){return this.normalized&&(n=vt(n,this.array)),this.array[e*this.itemSize+t]=n,this}getX(e){let t=this.array[e*this.itemSize];return this.normalized&&(t=li(t,this.array)),t}setX(e,t){return this.normalized&&(t=vt(t,this.array)),this.array[e*this.itemSize]=t,this}getY(e){let t=this.array[e*this.itemSize+1];return this.normalized&&(t=li(t,this.array)),t}setY(e,t){return this.normalized&&(t=vt(t,this.array)),this.array[e*this.itemSize+1]=t,this}getZ(e){let t=this.array[e*this.itemSize+2];return this.normalized&&(t=li(t,this.array)),t}setZ(e,t){return this.normalized&&(t=vt(t,this.array)),this.array[e*this.itemSize+2]=t,this}getW(e){let t=this.array[e*this.itemSize+3];return this.normalized&&(t=li(t,this.array)),t}setW(e,t){return this.normalized&&(t=vt(t,this.array)),this.array[e*this.itemSize+3]=t,this}setXY(e,t,n){return e*=this.itemSize,this.normalized&&(t=vt(t,this.array),n=vt(n,this.array)),this.array[e+0]=t,this.array[e+1]=n,this}setXYZ(e,t,n,r){return e*=this.itemSize,this.normalized&&(t=vt(t,this.array),n=vt(n,this.array),r=vt(r,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=r,this}setXYZW(e,t,n,r,s){return e*=this.itemSize,this.normalized&&(t=vt(t,this.array),n=vt(n,this.array),r=vt(r,this.array),s=vt(s,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=r,this.array[e+3]=s,this}onUpload(e){return this.onUploadCallback=e,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const e={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(e.name=this.name),this.usage!==Ma&&(e.usage=this.usage),e}}class zo extends Wt{constructor(e,t,n){super(new Uint16Array(e),t,n)}}class Ho extends Wt{constructor(e,t,n){super(new Uint32Array(e),t,n)}}class at extends Wt{constructor(e,t,n){super(new Float32Array(e),t,n)}}let Tc=0;const Pt=new et,Xr=new ht,kn=new D,At=new Ei,fi=new Ei,ct=new D;class Rt extends ii{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:Tc++}),this.uuid=Mi(),this.name="",this.type="BufferGeometry",this.index=null,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={}}getIndex(){return this.index}setIndex(e){return Array.isArray(e)?this.index=new(Uo(e)?Ho:zo)(e,1):this.index=e,this}getAttribute(e){return this.attributes[e]}setAttribute(e,t){return this.attributes[e]=t,this}deleteAttribute(e){return delete this.attributes[e],this}hasAttribute(e){return this.attributes[e]!==void 0}addGroup(e,t,n=0){this.groups.push({start:e,count:t,materialIndex:n})}clearGroups(){this.groups=[]}setDrawRange(e,t){this.drawRange.start=e,this.drawRange.count=t}applyMatrix4(e){const t=this.attributes.position;t!==void 0&&(t.applyMatrix4(e),t.needsUpdate=!0);const n=this.attributes.normal;if(n!==void 0){const s=new De().getNormalMatrix(e);n.applyNormalMatrix(s),n.needsUpdate=!0}const r=this.attributes.tangent;return r!==void 0&&(r.transformDirection(e),r.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this}applyQuaternion(e){return Pt.makeRotationFromQuaternion(e),this.applyMatrix4(Pt),this}rotateX(e){return Pt.makeRotationX(e),this.applyMatrix4(Pt),this}rotateY(e){return Pt.makeRotationY(e),this.applyMatrix4(Pt),this}rotateZ(e){return Pt.makeRotationZ(e),this.applyMatrix4(Pt),this}translate(e,t,n){return Pt.makeTranslation(e,t,n),this.applyMatrix4(Pt),this}scale(e,t,n){return Pt.makeScale(e,t,n),this.applyMatrix4(Pt),this}lookAt(e){return Xr.lookAt(e),Xr.updateMatrix(),this.applyMatrix4(Xr.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(kn).negate(),this.translate(kn.x,kn.y,kn.z),this}setFromPoints(e){const t=[];for(let n=0,r=e.length;n<r;n++){const s=e[n];t.push(s.x,s.y,s.z||0)}return this.setAttribute("position",new at(t,3)),this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new Ei);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){console.error("THREE.BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new D(-1/0,-1/0,-1/0),new D(1/0,1/0,1/0));return}if(e!==void 0){if(this.boundingBox.setFromBufferAttribute(e),t)for(let n=0,r=t.length;n<r;n++){const s=t[n];At.setFromBufferAttribute(s),this.morphTargetsRelative?(ct.addVectors(this.boundingBox.min,At.min),this.boundingBox.expandByPoint(ct),ct.addVectors(this.boundingBox.max,At.max),this.boundingBox.expandByPoint(ct)):(this.boundingBox.expandByPoint(At.min),this.boundingBox.expandByPoint(At.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&console.error('THREE.BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new mr);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){console.error("THREE.BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new D,1/0);return}if(e){const n=this.boundingSphere.center;if(At.setFromBufferAttribute(e),t)for(let s=0,a=t.length;s<a;s++){const o=t[s];fi.setFromBufferAttribute(o),this.morphTargetsRelative?(ct.addVectors(At.min,fi.min),At.expandByPoint(ct),ct.addVectors(At.max,fi.max),At.expandByPoint(ct)):(At.expandByPoint(fi.min),At.expandByPoint(fi.max))}At.getCenter(n);let r=0;for(let s=0,a=e.count;s<a;s++)ct.fromBufferAttribute(e,s),r=Math.max(r,n.distanceToSquared(ct));if(t)for(let s=0,a=t.length;s<a;s++){const o=t[s],l=this.morphTargetsRelative;for(let c=0,d=o.count;c<d;c++)ct.fromBufferAttribute(o,c),l&&(kn.fromBufferAttribute(e,c),ct.add(kn)),r=Math.max(r,n.distanceToSquared(ct))}this.boundingSphere.radius=Math.sqrt(r),isNaN(this.boundingSphere.radius)&&console.error('THREE.BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const e=this.index,t=this.attributes;if(e===null||t.position===void 0||t.normal===void 0||t.uv===void 0){console.error("THREE.BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const n=t.position,r=t.normal,s=t.uv;this.hasAttribute("tangent")===!1&&this.setAttribute("tangent",new Wt(new Float32Array(4*n.count),4));const a=this.getAttribute("tangent"),o=[],l=[];for(let I=0;I<n.count;I++)o[I]=new D,l[I]=new D;const c=new D,d=new D,p=new D,f=new ze,m=new ze,_=new ze,M=new D,h=new D;function u(I,K,g){c.fromBufferAttribute(n,I),d.fromBufferAttribute(n,K),p.fromBufferAttribute(n,g),f.fromBufferAttribute(s,I),m.fromBufferAttribute(s,K),_.fromBufferAttribute(s,g),d.sub(c),p.sub(c),m.sub(f),_.sub(f);const E=1/(m.x*_.y-_.x*m.y);isFinite(E)&&(M.copy(d).multiplyScalar(_.y).addScaledVector(p,-m.y).multiplyScalar(E),h.copy(p).multiplyScalar(m.x).addScaledVector(d,-_.x).multiplyScalar(E),o[I].add(M),o[K].add(M),o[g].add(M),l[I].add(h),l[K].add(h),l[g].add(h))}let y=this.groups;y.length===0&&(y=[{start:0,count:e.count}]);for(let I=0,K=y.length;I<K;++I){const g=y[I],E=g.start,k=g.count;for(let G=E,X=E+k;G<X;G+=3)u(e.getX(G+0),e.getX(G+1),e.getX(G+2))}const S=new D,T=new D,N=new D,R=new D;function w(I){N.fromBufferAttribute(r,I),R.copy(N);const K=o[I];S.copy(K),S.sub(N.multiplyScalar(N.dot(K))).normalize(),T.crossVectors(R,K);const E=T.dot(l[I])<0?-1:1;a.setXYZW(I,S.x,S.y,S.z,E)}for(let I=0,K=y.length;I<K;++I){const g=y[I],E=g.start,k=g.count;for(let G=E,X=E+k;G<X;G+=3)w(e.getX(G+0)),w(e.getX(G+1)),w(e.getX(G+2))}}computeVertexNormals(){const e=this.index,t=this.getAttribute("position");if(t!==void 0){let n=this.getAttribute("normal");if(n===void 0)n=new Wt(new Float32Array(t.count*3),3),this.setAttribute("normal",n);else for(let f=0,m=n.count;f<m;f++)n.setXYZ(f,0,0,0);const r=new D,s=new D,a=new D,o=new D,l=new D,c=new D,d=new D,p=new D;if(e)for(let f=0,m=e.count;f<m;f+=3){const _=e.getX(f+0),M=e.getX(f+1),h=e.getX(f+2);r.fromBufferAttribute(t,_),s.fromBufferAttribute(t,M),a.fromBufferAttribute(t,h),d.subVectors(a,s),p.subVectors(r,s),d.cross(p),o.fromBufferAttribute(n,_),l.fromBufferAttribute(n,M),c.fromBufferAttribute(n,h),o.add(d),l.add(d),c.add(d),n.setXYZ(_,o.x,o.y,o.z),n.setXYZ(M,l.x,l.y,l.z),n.setXYZ(h,c.x,c.y,c.z)}else for(let f=0,m=t.count;f<m;f+=3)r.fromBufferAttribute(t,f+0),s.fromBufferAttribute(t,f+1),a.fromBufferAttribute(t,f+2),d.subVectors(a,s),p.subVectors(r,s),d.cross(p),n.setXYZ(f+0,d.x,d.y,d.z),n.setXYZ(f+1,d.x,d.y,d.z),n.setXYZ(f+2,d.x,d.y,d.z);this.normalizeNormals(),n.needsUpdate=!0}}normalizeNormals(){const e=this.attributes.normal;for(let t=0,n=e.count;t<n;t++)ct.fromBufferAttribute(e,t),ct.normalize(),e.setXYZ(t,ct.x,ct.y,ct.z)}toNonIndexed(){function e(o,l){const c=o.array,d=o.itemSize,p=o.normalized,f=new c.constructor(l.length*d);let m=0,_=0;for(let M=0,h=l.length;M<h;M++){o.isInterleavedBufferAttribute?m=l[M]*o.data.stride+o.offset:m=l[M]*d;for(let u=0;u<d;u++)f[_++]=c[m++]}return new Wt(f,d,p)}if(this.index===null)return console.warn("THREE.BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const t=new Rt,n=this.index.array,r=this.attributes;for(const o in r){const l=r[o],c=e(l,n);t.setAttribute(o,c)}const s=this.morphAttributes;for(const o in s){const l=[],c=s[o];for(let d=0,p=c.length;d<p;d++){const f=c[d],m=e(f,n);l.push(m)}t.morphAttributes[o]=l}t.morphTargetsRelative=this.morphTargetsRelative;const a=this.groups;for(let o=0,l=a.length;o<l;o++){const c=a[o];t.addGroup(c.start,c.count,c.materialIndex)}return t}toJSON(){const e={metadata:{version:4.6,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(e.uuid=this.uuid,e.type=this.type,this.name!==""&&(e.name=this.name),Object.keys(this.userData).length>0&&(e.userData=this.userData),this.parameters!==void 0){const l=this.parameters;for(const c in l)l[c]!==void 0&&(e[c]=l[c]);return e}e.data={attributes:{}};const t=this.index;t!==null&&(e.data.index={type:t.array.constructor.name,array:Array.prototype.slice.call(t.array)});const n=this.attributes;for(const l in n){const c=n[l];e.data.attributes[l]=c.toJSON(e.data)}const r={};let s=!1;for(const l in this.morphAttributes){const c=this.morphAttributes[l],d=[];for(let p=0,f=c.length;p<f;p++){const m=c[p];d.push(m.toJSON(e.data))}d.length>0&&(r[l]=d,s=!0)}s&&(e.data.morphAttributes=r,e.data.morphTargetsRelative=this.morphTargetsRelative);const a=this.groups;a.length>0&&(e.data.groups=JSON.parse(JSON.stringify(a)));const o=this.boundingSphere;return o!==null&&(e.data.boundingSphere={center:o.center.toArray(),radius:o.radius}),e}clone(){return new this.constructor().copy(this)}copy(e){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const t={};this.name=e.name;const n=e.index;n!==null&&this.setIndex(n.clone(t));const r=e.attributes;for(const c in r){const d=r[c];this.setAttribute(c,d.clone(t))}const s=e.morphAttributes;for(const c in s){const d=[],p=s[c];for(let f=0,m=p.length;f<m;f++)d.push(p[f].clone(t));this.morphAttributes[c]=d}this.morphTargetsRelative=e.morphTargetsRelative;const a=e.groups;for(let c=0,d=a.length;c<d;c++){const p=a[c];this.addGroup(p.start,p.count,p.materialIndex)}const o=e.boundingBox;o!==null&&(this.boundingBox=o.clone());const l=e.boundingSphere;return l!==null&&(this.boundingSphere=l.clone()),this.drawRange.start=e.drawRange.start,this.drawRange.count=e.drawRange.count,this.userData=e.userData,this}dispose(){this.dispatchEvent({type:"dispose"})}}const Ia=new et,Mn=new Fo,Bi=new mr,Na=new D,zi=new D,Hi=new D,Gi=new D,qr=new D,Vi=new D,Fa=new D,ki=new D;class wt extends ht{constructor(e=new Rt,t=new cr){super(),this.isMesh=!0,this.type="Mesh",this.geometry=e,this.material=t,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),e.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=e.morphTargetInfluences.slice()),e.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},e.morphTargetDictionary)),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const r=t[n[0]];if(r!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=r.length;s<a;s++){const o=r[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}getVertexPosition(e,t){const n=this.geometry,r=n.attributes.position,s=n.morphAttributes.position,a=n.morphTargetsRelative;t.fromBufferAttribute(r,e);const o=this.morphTargetInfluences;if(s&&o){Vi.set(0,0,0);for(let l=0,c=s.length;l<c;l++){const d=o[l],p=s[l];d!==0&&(qr.fromBufferAttribute(p,e),a?Vi.addScaledVector(qr,d):Vi.addScaledVector(qr.sub(t),d))}t.add(Vi)}return t}raycast(e,t){const n=this.geometry,r=this.material,s=this.matrixWorld;r!==void 0&&(n.boundingSphere===null&&n.computeBoundingSphere(),Bi.copy(n.boundingSphere),Bi.applyMatrix4(s),Mn.copy(e.ray).recast(e.near),!(Bi.containsPoint(Mn.origin)===!1&&(Mn.intersectSphere(Bi,Na)===null||Mn.origin.distanceToSquared(Na)>(e.far-e.near)**2))&&(Ia.copy(s).invert(),Mn.copy(e.ray).applyMatrix4(Ia),!(n.boundingBox!==null&&Mn.intersectsBox(n.boundingBox)===!1)&&this._computeIntersections(e,t,Mn)))}_computeIntersections(e,t,n){let r;const s=this.geometry,a=this.material,o=s.index,l=s.attributes.position,c=s.attributes.uv,d=s.attributes.uv1,p=s.attributes.normal,f=s.groups,m=s.drawRange;if(o!==null)if(Array.isArray(a))for(let _=0,M=f.length;_<M;_++){const h=f[_],u=a[h.materialIndex],y=Math.max(h.start,m.start),S=Math.min(o.count,Math.min(h.start+h.count,m.start+m.count));for(let T=y,N=S;T<N;T+=3){const R=o.getX(T),w=o.getX(T+1),I=o.getX(T+2);r=Wi(this,u,e,n,c,d,p,R,w,I),r&&(r.faceIndex=Math.floor(T/3),r.face.materialIndex=h.materialIndex,t.push(r))}}else{const _=Math.max(0,m.start),M=Math.min(o.count,m.start+m.count);for(let h=_,u=M;h<u;h+=3){const y=o.getX(h),S=o.getX(h+1),T=o.getX(h+2);r=Wi(this,a,e,n,c,d,p,y,S,T),r&&(r.faceIndex=Math.floor(h/3),t.push(r))}}else if(l!==void 0)if(Array.isArray(a))for(let _=0,M=f.length;_<M;_++){const h=f[_],u=a[h.materialIndex],y=Math.max(h.start,m.start),S=Math.min(l.count,Math.min(h.start+h.count,m.start+m.count));for(let T=y,N=S;T<N;T+=3){const R=T,w=T+1,I=T+2;r=Wi(this,u,e,n,c,d,p,R,w,I),r&&(r.faceIndex=Math.floor(T/3),r.face.materialIndex=h.materialIndex,t.push(r))}}else{const _=Math.max(0,m.start),M=Math.min(l.count,m.start+m.count);for(let h=_,u=M;h<u;h+=3){const y=h,S=h+1,T=h+2;r=Wi(this,a,e,n,c,d,p,y,S,T),r&&(r.faceIndex=Math.floor(h/3),t.push(r))}}}}function Ac(i,e,t,n,r,s,a,o){let l;if(e.side===Mt?l=n.intersectTriangle(a,s,r,!0,o):l=n.intersectTriangle(r,s,a,e.side===pn,o),l===null)return null;ki.copy(o),ki.applyMatrix4(i.matrixWorld);const c=t.ray.origin.distanceTo(ki);return c<t.near||c>t.far?null:{distance:c,point:ki.clone(),object:i}}function Wi(i,e,t,n,r,s,a,o,l,c){i.getVertexPosition(o,zi),i.getVertexPosition(l,Hi),i.getVertexPosition(c,Gi);const d=Ac(i,e,t,n,zi,Hi,Gi,Fa);if(d){const p=new D;zt.getBarycoord(Fa,zi,Hi,Gi,p),r&&(d.uv=zt.getInterpolatedAttribute(r,o,l,c,p,new ze)),s&&(d.uv1=zt.getInterpolatedAttribute(s,o,l,c,p,new ze)),a&&(d.normal=zt.getInterpolatedAttribute(a,o,l,c,p,new D),d.normal.dot(n.direction)>0&&d.normal.multiplyScalar(-1));const f={a:o,b:l,c,normal:new D,materialIndex:0};zt.getNormal(zi,Hi,Gi,f.normal),d.face=f,d.barycoord=p}return d}class yi extends Rt{constructor(e=1,t=1,n=1,r=1,s=1,a=1){super(),this.type="BoxGeometry",this.parameters={width:e,height:t,depth:n,widthSegments:r,heightSegments:s,depthSegments:a};const o=this;r=Math.floor(r),s=Math.floor(s),a=Math.floor(a);const l=[],c=[],d=[],p=[];let f=0,m=0;_("z","y","x",-1,-1,n,t,e,a,s,0),_("z","y","x",1,-1,n,t,-e,a,s,1),_("x","z","y",1,1,e,n,t,r,a,2),_("x","z","y",1,-1,e,n,-t,r,a,3),_("x","y","z",1,-1,e,t,n,r,s,4),_("x","y","z",-1,-1,e,t,-n,r,s,5),this.setIndex(l),this.setAttribute("position",new at(c,3)),this.setAttribute("normal",new at(d,3)),this.setAttribute("uv",new at(p,2));function _(M,h,u,y,S,T,N,R,w,I,K){const g=T/w,E=N/I,k=T/2,G=N/2,X=R/2,J=w+1,H=I+1;let Q=0,W=0;const le=new D;for(let ce=0;ce<H;ce++){const _e=ce*E-G;for(let He=0;He<J;He++){const Ve=He*g-k;le[M]=Ve*y,le[h]=_e*S,le[u]=X,c.push(le.x,le.y,le.z),le[M]=0,le[h]=0,le[u]=R>0?1:-1,d.push(le.x,le.y,le.z),p.push(He/w),p.push(1-ce/I),Q+=1}}for(let ce=0;ce<I;ce++)for(let _e=0;_e<w;_e++){const He=f+_e+J*ce,Ve=f+_e+J*(ce+1),b=f+(_e+1)+J*(ce+1),B=f+(_e+1)+J*ce;l.push(He,Ve,B),l.push(Ve,b,B),W+=6}o.addGroup(m,W,K),m+=W,f+=Q}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new yi(e.width,e.height,e.depth,e.widthSegments,e.heightSegments,e.depthSegments)}}function ni(i){const e={};for(const t in i){e[t]={};for(const n in i[t]){const r=i[t][n];r&&(r.isColor||r.isMatrix3||r.isMatrix4||r.isVector2||r.isVector3||r.isVector4||r.isTexture||r.isQuaternion)?r.isRenderTargetTexture?(console.warn("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),e[t][n]=null):e[t][n]=r.clone():Array.isArray(r)?e[t][n]=r.slice():e[t][n]=r}}return e}function pt(i){const e={};for(let t=0;t<i.length;t++){const n=ni(i[t]);for(const r in n)e[r]=n[r]}return e}function bc(i){const e=[];for(let t=0;t<i.length;t++)e.push(i[t].clone());return e}function Go(i){const e=i.getRenderTarget();return e===null?i.outputColorSpace:e.isXRRenderTarget===!0?e.texture.colorSpace:Ke.workingColorSpace}const wc={clone:ni,merge:pt};var Rc=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,Cc=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class mn extends ri{constructor(e){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=Rc,this.fragmentShader=Cc,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,e!==void 0&&this.setValues(e)}copy(e){return super.copy(e),this.fragmentShader=e.fragmentShader,this.vertexShader=e.vertexShader,this.uniforms=ni(e.uniforms),this.uniformsGroups=bc(e.uniformsGroups),this.defines=Object.assign({},e.defines),this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.fog=e.fog,this.lights=e.lights,this.clipping=e.clipping,this.extensions=Object.assign({},e.extensions),this.glslVersion=e.glslVersion,this}toJSON(e){const t=super.toJSON(e);t.glslVersion=this.glslVersion,t.uniforms={};for(const r in this.uniforms){const a=this.uniforms[r].value;a&&a.isTexture?t.uniforms[r]={type:"t",value:a.toJSON(e).uuid}:a&&a.isColor?t.uniforms[r]={type:"c",value:a.getHex()}:a&&a.isVector2?t.uniforms[r]={type:"v2",value:a.toArray()}:a&&a.isVector3?t.uniforms[r]={type:"v3",value:a.toArray()}:a&&a.isVector4?t.uniforms[r]={type:"v4",value:a.toArray()}:a&&a.isMatrix3?t.uniforms[r]={type:"m3",value:a.toArray()}:a&&a.isMatrix4?t.uniforms[r]={type:"m4",value:a.toArray()}:t.uniforms[r]={value:a}}Object.keys(this.defines).length>0&&(t.defines=this.defines),t.vertexShader=this.vertexShader,t.fragmentShader=this.fragmentShader,t.lights=this.lights,t.clipping=this.clipping;const n={};for(const r in this.extensions)this.extensions[r]===!0&&(n[r]=!0);return Object.keys(n).length>0&&(t.extensions=n),t}}class Vo extends ht{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new et,this.projectionMatrix=new et,this.projectionMatrixInverse=new et,this.coordinateSystem=tn}copy(e,t){return super.copy(e,t),this.matrixWorldInverse.copy(e.matrixWorldInverse),this.projectionMatrix.copy(e.projectionMatrix),this.projectionMatrixInverse.copy(e.projectionMatrixInverse),this.coordinateSystem=e.coordinateSystem,this}getWorldDirection(e){return super.getWorldDirection(e).negate()}updateMatrixWorld(e){super.updateMatrixWorld(e),this.matrixWorldInverse.copy(this.matrixWorld).invert()}updateWorldMatrix(e,t){super.updateWorldMatrix(e,t),this.matrixWorldInverse.copy(this.matrixWorld).invert()}clone(){return new this.constructor().copy(this)}}const un=new D,Oa=new ze,Ba=new ze;class bt extends Vo{constructor(e=50,t=1,n=.1,r=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=e,this.zoom=1,this.near=n,this.far=r,this.focus=10,this.aspect=t,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.fov=e.fov,this.zoom=e.zoom,this.near=e.near,this.far=e.far,this.focus=e.focus,this.aspect=e.aspect,this.view=e.view===null?null:Object.assign({},e.view),this.filmGauge=e.filmGauge,this.filmOffset=e.filmOffset,this}setFocalLength(e){const t=.5*this.getFilmHeight()/e;this.fov=ks*2*Math.atan(t),this.updateProjectionMatrix()}getFocalLength(){const e=Math.tan(Ar*.5*this.fov);return .5*this.getFilmHeight()/e}getEffectiveFOV(){return ks*2*Math.atan(Math.tan(Ar*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(e,t,n){un.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),t.set(un.x,un.y).multiplyScalar(-e/un.z),un.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),n.set(un.x,un.y).multiplyScalar(-e/un.z)}getViewSize(e,t){return this.getViewBounds(e,Oa,Ba),t.subVectors(Ba,Oa)}setViewOffset(e,t,n,r,s,a){this.aspect=e/t,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=r,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=this.near;let t=e*Math.tan(Ar*.5*this.fov)/this.zoom,n=2*t,r=this.aspect*n,s=-.5*r;const a=this.view;if(this.view!==null&&this.view.enabled){const l=a.fullWidth,c=a.fullHeight;s+=a.offsetX*r/l,t-=a.offsetY*n/c,r*=a.width/l,n*=a.height/c}const o=this.filmOffset;o!==0&&(s+=e*o/this.getFilmWidth()),this.projectionMatrix.makePerspective(s,s+r,t,t-n,e,this.far,this.coordinateSystem),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.fov=this.fov,t.object.zoom=this.zoom,t.object.near=this.near,t.object.far=this.far,t.object.focus=this.focus,t.object.aspect=this.aspect,this.view!==null&&(t.object.view=Object.assign({},this.view)),t.object.filmGauge=this.filmGauge,t.object.filmOffset=this.filmOffset,t}}const Wn=-90,Xn=1;class Pc extends ht{constructor(e,t,n){super(),this.type="CubeCamera",this.renderTarget=n,this.coordinateSystem=null,this.activeMipmapLevel=0;const r=new bt(Wn,Xn,e,t);r.layers=this.layers,this.add(r);const s=new bt(Wn,Xn,e,t);s.layers=this.layers,this.add(s);const a=new bt(Wn,Xn,e,t);a.layers=this.layers,this.add(a);const o=new bt(Wn,Xn,e,t);o.layers=this.layers,this.add(o);const l=new bt(Wn,Xn,e,t);l.layers=this.layers,this.add(l);const c=new bt(Wn,Xn,e,t);c.layers=this.layers,this.add(c)}updateCoordinateSystem(){const e=this.coordinateSystem,t=this.children.concat(),[n,r,s,a,o,l]=t;for(const c of t)this.remove(c);if(e===tn)n.up.set(0,1,0),n.lookAt(1,0,0),r.up.set(0,1,0),r.lookAt(-1,0,0),s.up.set(0,0,-1),s.lookAt(0,1,0),a.up.set(0,0,1),a.lookAt(0,-1,0),o.up.set(0,1,0),o.lookAt(0,0,1),l.up.set(0,1,0),l.lookAt(0,0,-1);else if(e===or)n.up.set(0,-1,0),n.lookAt(-1,0,0),r.up.set(0,-1,0),r.lookAt(1,0,0),s.up.set(0,0,1),s.lookAt(0,1,0),a.up.set(0,0,-1),a.lookAt(0,-1,0),o.up.set(0,-1,0),o.lookAt(0,0,1),l.up.set(0,-1,0),l.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+e);for(const c of t)this.add(c),c.updateMatrixWorld()}update(e,t){this.parent===null&&this.updateMatrixWorld();const{renderTarget:n,activeMipmapLevel:r}=this;this.coordinateSystem!==e.coordinateSystem&&(this.coordinateSystem=e.coordinateSystem,this.updateCoordinateSystem());const[s,a,o,l,c,d]=this.children,p=e.getRenderTarget(),f=e.getActiveCubeFace(),m=e.getActiveMipmapLevel(),_=e.xr.enabled;e.xr.enabled=!1;const M=n.texture.generateMipmaps;n.texture.generateMipmaps=!1,e.setRenderTarget(n,0,r),e.render(t,s),e.setRenderTarget(n,1,r),e.render(t,a),e.setRenderTarget(n,2,r),e.render(t,o),e.setRenderTarget(n,3,r),e.render(t,l),e.setRenderTarget(n,4,r),e.render(t,c),n.texture.generateMipmaps=M,e.setRenderTarget(n,5,r),e.render(t,d),e.setRenderTarget(p,f,m),e.xr.enabled=_,n.texture.needsPMREMUpdate=!0}}class ko extends St{constructor(e,t,n,r,s,a,o,l,c,d){e=e!==void 0?e:[],t=t!==void 0?t:Jn,super(e,t,n,r,s,a,o,l,c,d),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(e){this.image=e}}class Lc extends Ln{constructor(e=1,t={}){super(e,e,t),this.isWebGLCubeRenderTarget=!0;const n={width:e,height:e,depth:1},r=[n,n,n,n,n,n];this.texture=new ko(r,t.mapping,t.wrapS,t.wrapT,t.magFilter,t.minFilter,t.format,t.type,t.anisotropy,t.colorSpace),this.texture.isRenderTargetTexture=!0,this.texture.generateMipmaps=t.generateMipmaps!==void 0?t.generateMipmaps:!1,this.texture.minFilter=t.minFilter!==void 0?t.minFilter:Bt}fromEquirectangularTexture(e,t){this.texture.type=t.type,this.texture.colorSpace=t.colorSpace,this.texture.generateMipmaps=t.generateMipmaps,this.texture.minFilter=t.minFilter,this.texture.magFilter=t.magFilter;const n={uniforms:{tEquirect:{value:null}},vertexShader:`

				varying vec3 vWorldDirection;

				vec3 transformDirection( in vec3 dir, in mat4 matrix ) {

					return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );

				}

				void main() {

					vWorldDirection = transformDirection( position, modelMatrix );

					#include <begin_vertex>
					#include <project_vertex>

				}
			`,fragmentShader:`

				uniform sampler2D tEquirect;

				varying vec3 vWorldDirection;

				#include <common>

				void main() {

					vec3 direction = normalize( vWorldDirection );

					vec2 sampleUV = equirectUv( direction );

					gl_FragColor = texture2D( tEquirect, sampleUV );

				}
			`},r=new yi(5,5,5),s=new mn({name:"CubemapFromEquirect",uniforms:ni(n.uniforms),vertexShader:n.vertexShader,fragmentShader:n.fragmentShader,side:Mt,blending:dn});s.uniforms.tEquirect.value=t;const a=new wt(r,s),o=t.minFilter;return t.minFilter===Cn&&(t.minFilter=Bt),new Pc(1,10,this).update(e,a),t.minFilter=o,a.geometry.dispose(),a.material.dispose(),this}clear(e,t,n,r){const s=e.getRenderTarget();for(let a=0;a<6;a++)e.setRenderTarget(this,a),e.clear(t,n,r);e.setRenderTarget(s)}}const Yr=new D,Dc=new D,Uc=new De;class Tn{constructor(e=new D(1,0,0),t=0){this.isPlane=!0,this.normal=e,this.constant=t}set(e,t){return this.normal.copy(e),this.constant=t,this}setComponents(e,t,n,r){return this.normal.set(e,t,n),this.constant=r,this}setFromNormalAndCoplanarPoint(e,t){return this.normal.copy(e),this.constant=-t.dot(this.normal),this}setFromCoplanarPoints(e,t,n){const r=Yr.subVectors(n,t).cross(Dc.subVectors(e,t)).normalize();return this.setFromNormalAndCoplanarPoint(r,e),this}copy(e){return this.normal.copy(e.normal),this.constant=e.constant,this}normalize(){const e=1/this.normal.length();return this.normal.multiplyScalar(e),this.constant*=e,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(e){return this.normal.dot(e)+this.constant}distanceToSphere(e){return this.distanceToPoint(e.center)-e.radius}projectPoint(e,t){return t.copy(e).addScaledVector(this.normal,-this.distanceToPoint(e))}intersectLine(e,t){const n=e.delta(Yr),r=this.normal.dot(n);if(r===0)return this.distanceToPoint(e.start)===0?t.copy(e.start):null;const s=-(e.start.dot(this.normal)+this.constant)/r;return s<0||s>1?null:t.copy(e.start).addScaledVector(n,s)}intersectsLine(e){const t=this.distanceToPoint(e.start),n=this.distanceToPoint(e.end);return t<0&&n>0||n<0&&t>0}intersectsBox(e){return e.intersectsPlane(this)}intersectsSphere(e){return e.intersectsPlane(this)}coplanarPoint(e){return e.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(e,t){const n=t||Uc.getNormalMatrix(e),r=this.coplanarPoint(Yr).applyMatrix4(e),s=this.normal.applyMatrix3(n).normalize();return this.constant=-r.dot(s),this}translate(e){return this.constant-=e.dot(this.normal),this}equals(e){return e.normal.equals(this.normal)&&e.constant===this.constant}clone(){return new this.constructor().copy(this)}}const Sn=new mr,Xi=new D;class Qs{constructor(e=new Tn,t=new Tn,n=new Tn,r=new Tn,s=new Tn,a=new Tn){this.planes=[e,t,n,r,s,a]}set(e,t,n,r,s,a){const o=this.planes;return o[0].copy(e),o[1].copy(t),o[2].copy(n),o[3].copy(r),o[4].copy(s),o[5].copy(a),this}copy(e){const t=this.planes;for(let n=0;n<6;n++)t[n].copy(e.planes[n]);return this}setFromProjectionMatrix(e,t=tn){const n=this.planes,r=e.elements,s=r[0],a=r[1],o=r[2],l=r[3],c=r[4],d=r[5],p=r[6],f=r[7],m=r[8],_=r[9],M=r[10],h=r[11],u=r[12],y=r[13],S=r[14],T=r[15];if(n[0].setComponents(l-s,f-c,h-m,T-u).normalize(),n[1].setComponents(l+s,f+c,h+m,T+u).normalize(),n[2].setComponents(l+a,f+d,h+_,T+y).normalize(),n[3].setComponents(l-a,f-d,h-_,T-y).normalize(),n[4].setComponents(l-o,f-p,h-M,T-S).normalize(),t===tn)n[5].setComponents(l+o,f+p,h+M,T+S).normalize();else if(t===or)n[5].setComponents(o,p,M,S).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+t);return this}intersectsObject(e){if(e.boundingSphere!==void 0)e.boundingSphere===null&&e.computeBoundingSphere(),Sn.copy(e.boundingSphere).applyMatrix4(e.matrixWorld);else{const t=e.geometry;t.boundingSphere===null&&t.computeBoundingSphere(),Sn.copy(t.boundingSphere).applyMatrix4(e.matrixWorld)}return this.intersectsSphere(Sn)}intersectsSprite(e){return Sn.center.set(0,0,0),Sn.radius=.7071067811865476,Sn.applyMatrix4(e.matrixWorld),this.intersectsSphere(Sn)}intersectsSphere(e){const t=this.planes,n=e.center,r=-e.radius;for(let s=0;s<6;s++)if(t[s].distanceToPoint(n)<r)return!1;return!0}intersectsBox(e){const t=this.planes;for(let n=0;n<6;n++){const r=t[n];if(Xi.x=r.normal.x>0?e.max.x:e.min.x,Xi.y=r.normal.y>0?e.max.y:e.min.y,Xi.z=r.normal.z>0?e.max.z:e.min.z,r.distanceToPoint(Xi)<0)return!1}return!0}containsPoint(e){const t=this.planes;for(let n=0;n<6;n++)if(t[n].distanceToPoint(e)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}function Wo(){let i=null,e=!1,t=null,n=null;function r(s,a){t(s,a),n=i.requestAnimationFrame(r)}return{start:function(){e!==!0&&t!==null&&(n=i.requestAnimationFrame(r),e=!0)},stop:function(){i.cancelAnimationFrame(n),e=!1},setAnimationLoop:function(s){t=s},setContext:function(s){i=s}}}function Ic(i){const e=new WeakMap;function t(o,l){const c=o.array,d=o.usage,p=c.byteLength,f=i.createBuffer();i.bindBuffer(l,f),i.bufferData(l,c,d),o.onUploadCallback();let m;if(c instanceof Float32Array)m=i.FLOAT;else if(c instanceof Uint16Array)o.isFloat16BufferAttribute?m=i.HALF_FLOAT:m=i.UNSIGNED_SHORT;else if(c instanceof Int16Array)m=i.SHORT;else if(c instanceof Uint32Array)m=i.UNSIGNED_INT;else if(c instanceof Int32Array)m=i.INT;else if(c instanceof Int8Array)m=i.BYTE;else if(c instanceof Uint8Array)m=i.UNSIGNED_BYTE;else if(c instanceof Uint8ClampedArray)m=i.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+c);return{buffer:f,type:m,bytesPerElement:c.BYTES_PER_ELEMENT,version:o.version,size:p}}function n(o,l,c){const d=l.array,p=l.updateRanges;if(i.bindBuffer(c,o),p.length===0)i.bufferSubData(c,0,d);else{p.sort((m,_)=>m.start-_.start);let f=0;for(let m=1;m<p.length;m++){const _=p[f],M=p[m];M.start<=_.start+_.count+1?_.count=Math.max(_.count,M.start+M.count-_.start):(++f,p[f]=M)}p.length=f+1;for(let m=0,_=p.length;m<_;m++){const M=p[m];i.bufferSubData(c,M.start*d.BYTES_PER_ELEMENT,d,M.start,M.count)}l.clearUpdateRanges()}l.onUploadCallback()}function r(o){return o.isInterleavedBufferAttribute&&(o=o.data),e.get(o)}function s(o){o.isInterleavedBufferAttribute&&(o=o.data);const l=e.get(o);l&&(i.deleteBuffer(l.buffer),e.delete(o))}function a(o,l){if(o.isInterleavedBufferAttribute&&(o=o.data),o.isGLBufferAttribute){const d=e.get(o);(!d||d.version<o.version)&&e.set(o,{buffer:o.buffer,type:o.type,bytesPerElement:o.elementSize,version:o.version});return}const c=e.get(o);if(c===void 0)e.set(o,t(o,l));else if(c.version<o.version){if(c.size!==o.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");n(c.buffer,o,l),c.version=o.version}}return{get:r,remove:s,update:a}}class _r extends Rt{constructor(e=1,t=1,n=1,r=1){super(),this.type="PlaneGeometry",this.parameters={width:e,height:t,widthSegments:n,heightSegments:r};const s=e/2,a=t/2,o=Math.floor(n),l=Math.floor(r),c=o+1,d=l+1,p=e/o,f=t/l,m=[],_=[],M=[],h=[];for(let u=0;u<d;u++){const y=u*f-a;for(let S=0;S<c;S++){const T=S*p-s;_.push(T,-y,0),M.push(0,0,1),h.push(S/o),h.push(1-u/l)}}for(let u=0;u<l;u++)for(let y=0;y<o;y++){const S=y+c*u,T=y+c*(u+1),N=y+1+c*(u+1),R=y+1+c*u;m.push(S,T,R),m.push(T,N,R)}this.setIndex(m),this.setAttribute("position",new at(_,3)),this.setAttribute("normal",new at(M,3)),this.setAttribute("uv",new at(h,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new _r(e.width,e.height,e.widthSegments,e.heightSegments)}}var Nc=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,Fc=`#ifdef USE_ALPHAHASH
	const float ALPHA_HASH_SCALE = 0.05;
	float hash2D( vec2 value ) {
		return fract( 1.0e4 * sin( 17.0 * value.x + 0.1 * value.y ) * ( 0.1 + abs( sin( 13.0 * value.y + value.x ) ) ) );
	}
	float hash3D( vec3 value ) {
		return hash2D( vec2( hash2D( value.xy ), value.z ) );
	}
	float getAlphaHashThreshold( vec3 position ) {
		float maxDeriv = max(
			length( dFdx( position.xyz ) ),
			length( dFdy( position.xyz ) )
		);
		float pixScale = 1.0 / ( ALPHA_HASH_SCALE * maxDeriv );
		vec2 pixScales = vec2(
			exp2( floor( log2( pixScale ) ) ),
			exp2( ceil( log2( pixScale ) ) )
		);
		vec2 alpha = vec2(
			hash3D( floor( pixScales.x * position.xyz ) ),
			hash3D( floor( pixScales.y * position.xyz ) )
		);
		float lerpFactor = fract( log2( pixScale ) );
		float x = ( 1.0 - lerpFactor ) * alpha.x + lerpFactor * alpha.y;
		float a = min( lerpFactor, 1.0 - lerpFactor );
		vec3 cases = vec3(
			x * x / ( 2.0 * a * ( 1.0 - a ) ),
			( x - 0.5 * a ) / ( 1.0 - a ),
			1.0 - ( ( 1.0 - x ) * ( 1.0 - x ) / ( 2.0 * a * ( 1.0 - a ) ) )
		);
		float threshold = ( x < ( 1.0 - a ) )
			? ( ( x < a ) ? cases.x : cases.y )
			: cases.z;
		return clamp( threshold , 1.0e-6, 1.0 );
	}
#endif`,Oc=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,Bc=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,zc=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,Hc=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,Gc=`#ifdef USE_AOMAP
	float ambientOcclusion = ( texture2D( aoMap, vAoMapUv ).r - 1.0 ) * aoMapIntensity + 1.0;
	reflectedLight.indirectDiffuse *= ambientOcclusion;
	#if defined( USE_CLEARCOAT ) 
		clearcoatSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_SHEEN ) 
		sheenSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_ENVMAP ) && defined( STANDARD )
		float dotNV = saturate( dot( geometryNormal, geometryViewDir ) );
		reflectedLight.indirectSpecular *= computeSpecularOcclusion( dotNV, ambientOcclusion, material.roughness );
	#endif
#endif`,Vc=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,kc=`#ifdef USE_BATCHING
	#if ! defined( GL_ANGLE_multi_draw )
	#define gl_DrawID _gl_DrawID
	uniform int _gl_DrawID;
	#endif
	uniform highp sampler2D batchingTexture;
	uniform highp usampler2D batchingIdTexture;
	mat4 getBatchingMatrix( const in float i ) {
		int size = textureSize( batchingTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( batchingTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( batchingTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( batchingTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( batchingTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
	float getIndirectIndex( const in int i ) {
		int size = textureSize( batchingIdTexture, 0 ).x;
		int x = i % size;
		int y = i / size;
		return float( texelFetch( batchingIdTexture, ivec2( x, y ), 0 ).r );
	}
#endif
#ifdef USE_BATCHING_COLOR
	uniform sampler2D batchingColorTexture;
	vec3 getBatchingColor( const in float i ) {
		int size = textureSize( batchingColorTexture, 0 ).x;
		int j = int( i );
		int x = j % size;
		int y = j / size;
		return texelFetch( batchingColorTexture, ivec2( x, y ), 0 ).rgb;
	}
#endif`,Wc=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,Xc=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,qc=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,Yc=`float G_BlinnPhong_Implicit( ) {
	return 0.25;
}
float D_BlinnPhong( const in float shininess, const in float dotNH ) {
	return RECIPROCAL_PI * ( shininess * 0.5 + 1.0 ) * pow( dotNH, shininess );
}
vec3 BRDF_BlinnPhong( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in vec3 specularColor, const in float shininess ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( specularColor, 1.0, dotVH );
	float G = G_BlinnPhong_Implicit( );
	float D = D_BlinnPhong( shininess, dotNH );
	return F * ( G * D );
} // validated`,Kc=`#ifdef USE_IRIDESCENCE
	const mat3 XYZ_TO_REC709 = mat3(
		 3.2404542, -0.9692660,  0.0556434,
		-1.5371385,  1.8760108, -0.2040259,
		-0.4985314,  0.0415560,  1.0572252
	);
	vec3 Fresnel0ToIor( vec3 fresnel0 ) {
		vec3 sqrtF0 = sqrt( fresnel0 );
		return ( vec3( 1.0 ) + sqrtF0 ) / ( vec3( 1.0 ) - sqrtF0 );
	}
	vec3 IorToFresnel0( vec3 transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - vec3( incidentIor ) ) / ( transmittedIor + vec3( incidentIor ) ) );
	}
	float IorToFresnel0( float transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - incidentIor ) / ( transmittedIor + incidentIor ));
	}
	vec3 evalSensitivity( float OPD, vec3 shift ) {
		float phase = 2.0 * PI * OPD * 1.0e-9;
		vec3 val = vec3( 5.4856e-13, 4.4201e-13, 5.2481e-13 );
		vec3 pos = vec3( 1.6810e+06, 1.7953e+06, 2.2084e+06 );
		vec3 var = vec3( 4.3278e+09, 9.3046e+09, 6.6121e+09 );
		vec3 xyz = val * sqrt( 2.0 * PI * var ) * cos( pos * phase + shift ) * exp( - pow2( phase ) * var );
		xyz.x += 9.7470e-14 * sqrt( 2.0 * PI * 4.5282e+09 ) * cos( 2.2399e+06 * phase + shift[ 0 ] ) * exp( - 4.5282e+09 * pow2( phase ) );
		xyz /= 1.0685e-7;
		vec3 rgb = XYZ_TO_REC709 * xyz;
		return rgb;
	}
	vec3 evalIridescence( float outsideIOR, float eta2, float cosTheta1, float thinFilmThickness, vec3 baseF0 ) {
		vec3 I;
		float iridescenceIOR = mix( outsideIOR, eta2, smoothstep( 0.0, 0.03, thinFilmThickness ) );
		float sinTheta2Sq = pow2( outsideIOR / iridescenceIOR ) * ( 1.0 - pow2( cosTheta1 ) );
		float cosTheta2Sq = 1.0 - sinTheta2Sq;
		if ( cosTheta2Sq < 0.0 ) {
			return vec3( 1.0 );
		}
		float cosTheta2 = sqrt( cosTheta2Sq );
		float R0 = IorToFresnel0( iridescenceIOR, outsideIOR );
		float R12 = F_Schlick( R0, 1.0, cosTheta1 );
		float T121 = 1.0 - R12;
		float phi12 = 0.0;
		if ( iridescenceIOR < outsideIOR ) phi12 = PI;
		float phi21 = PI - phi12;
		vec3 baseIOR = Fresnel0ToIor( clamp( baseF0, 0.0, 0.9999 ) );		vec3 R1 = IorToFresnel0( baseIOR, iridescenceIOR );
		vec3 R23 = F_Schlick( R1, 1.0, cosTheta2 );
		vec3 phi23 = vec3( 0.0 );
		if ( baseIOR[ 0 ] < iridescenceIOR ) phi23[ 0 ] = PI;
		if ( baseIOR[ 1 ] < iridescenceIOR ) phi23[ 1 ] = PI;
		if ( baseIOR[ 2 ] < iridescenceIOR ) phi23[ 2 ] = PI;
		float OPD = 2.0 * iridescenceIOR * thinFilmThickness * cosTheta2;
		vec3 phi = vec3( phi21 ) + phi23;
		vec3 R123 = clamp( R12 * R23, 1e-5, 0.9999 );
		vec3 r123 = sqrt( R123 );
		vec3 Rs = pow2( T121 ) * R23 / ( vec3( 1.0 ) - R123 );
		vec3 C0 = R12 + Rs;
		I = C0;
		vec3 Cm = Rs - T121;
		for ( int m = 1; m <= 2; ++ m ) {
			Cm *= r123;
			vec3 Sm = 2.0 * evalSensitivity( float( m ) * OPD, float( m ) * phi );
			I += Cm * Sm;
		}
		return max( I, vec3( 0.0 ) );
	}
#endif`,$c=`#ifdef USE_BUMPMAP
	uniform sampler2D bumpMap;
	uniform float bumpScale;
	vec2 dHdxy_fwd() {
		vec2 dSTdx = dFdx( vBumpMapUv );
		vec2 dSTdy = dFdy( vBumpMapUv );
		float Hll = bumpScale * texture2D( bumpMap, vBumpMapUv ).x;
		float dBx = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdx ).x - Hll;
		float dBy = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdy ).x - Hll;
		return vec2( dBx, dBy );
	}
	vec3 perturbNormalArb( vec3 surf_pos, vec3 surf_norm, vec2 dHdxy, float faceDirection ) {
		vec3 vSigmaX = normalize( dFdx( surf_pos.xyz ) );
		vec3 vSigmaY = normalize( dFdy( surf_pos.xyz ) );
		vec3 vN = surf_norm;
		vec3 R1 = cross( vSigmaY, vN );
		vec3 R2 = cross( vN, vSigmaX );
		float fDet = dot( vSigmaX, R1 ) * faceDirection;
		vec3 vGrad = sign( fDet ) * ( dHdxy.x * R1 + dHdxy.y * R2 );
		return normalize( abs( fDet ) * surf_norm - vGrad );
	}
#endif`,Zc=`#if NUM_CLIPPING_PLANES > 0
	vec4 plane;
	#ifdef ALPHA_TO_COVERAGE
		float distanceToPlane, distanceGradient;
		float clipOpacity = 1.0;
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
			distanceGradient = fwidth( distanceToPlane ) / 2.0;
			clipOpacity *= smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			if ( clipOpacity == 0.0 ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			float unionClipOpacity = 1.0;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
				distanceGradient = fwidth( distanceToPlane ) / 2.0;
				unionClipOpacity *= 1.0 - smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			}
			#pragma unroll_loop_end
			clipOpacity *= 1.0 - unionClipOpacity;
		#endif
		diffuseColor.a *= clipOpacity;
		if ( diffuseColor.a == 0.0 ) discard;
	#else
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			if ( dot( vClipPosition, plane.xyz ) > plane.w ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			bool clipped = true;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				clipped = ( dot( vClipPosition, plane.xyz ) > plane.w ) && clipped;
			}
			#pragma unroll_loop_end
			if ( clipped ) discard;
		#endif
	#endif
#endif`,jc=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,Jc=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,Qc=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,eu=`#if defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#elif defined( USE_COLOR )
	diffuseColor.rgb *= vColor;
#endif`,tu=`#if defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#elif defined( USE_COLOR )
	varying vec3 vColor;
#endif`,nu=`#if defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#elif defined( USE_COLOR ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec3 vColor;
#endif`,iu=`#if defined( USE_COLOR_ALPHA )
	vColor = vec4( 1.0 );
#elif defined( USE_COLOR ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	vColor = vec3( 1.0 );
#endif
#ifdef USE_COLOR
	vColor *= color;
#endif
#ifdef USE_INSTANCING_COLOR
	vColor.xyz *= instanceColor.xyz;
#endif
#ifdef USE_BATCHING_COLOR
	vec3 batchingColor = getBatchingColor( getIndirectIndex( gl_DrawID ) );
	vColor.xyz *= batchingColor.xyz;
#endif`,ru=`#define PI 3.141592653589793
#define PI2 6.283185307179586
#define PI_HALF 1.5707963267948966
#define RECIPROCAL_PI 0.3183098861837907
#define RECIPROCAL_PI2 0.15915494309189535
#define EPSILON 1e-6
#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
#define whiteComplement( a ) ( 1.0 - saturate( a ) )
float pow2( const in float x ) { return x*x; }
vec3 pow2( const in vec3 x ) { return x*x; }
float pow3( const in float x ) { return x*x*x; }
float pow4( const in float x ) { float x2 = x*x; return x2*x2; }
float max3( const in vec3 v ) { return max( max( v.x, v.y ), v.z ); }
float average( const in vec3 v ) { return dot( v, vec3( 0.3333333 ) ); }
highp float rand( const in vec2 uv ) {
	const highp float a = 12.9898, b = 78.233, c = 43758.5453;
	highp float dt = dot( uv.xy, vec2( a,b ) ), sn = mod( dt, PI );
	return fract( sin( sn ) * c );
}
#ifdef HIGH_PRECISION
	float precisionSafeLength( vec3 v ) { return length( v ); }
#else
	float precisionSafeLength( vec3 v ) {
		float maxComponent = max3( abs( v ) );
		return length( v / maxComponent ) * maxComponent;
	}
#endif
struct IncidentLight {
	vec3 color;
	vec3 direction;
	bool visible;
};
struct ReflectedLight {
	vec3 directDiffuse;
	vec3 directSpecular;
	vec3 indirectDiffuse;
	vec3 indirectSpecular;
};
#ifdef USE_ALPHAHASH
	varying vec3 vPosition;
#endif
vec3 transformDirection( in vec3 dir, in mat4 matrix ) {
	return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );
}
vec3 inverseTransformDirection( in vec3 dir, in mat4 matrix ) {
	return normalize( ( vec4( dir, 0.0 ) * matrix ).xyz );
}
mat3 transposeMat3( const in mat3 m ) {
	mat3 tmp;
	tmp[ 0 ] = vec3( m[ 0 ].x, m[ 1 ].x, m[ 2 ].x );
	tmp[ 1 ] = vec3( m[ 0 ].y, m[ 1 ].y, m[ 2 ].y );
	tmp[ 2 ] = vec3( m[ 0 ].z, m[ 1 ].z, m[ 2 ].z );
	return tmp;
}
bool isPerspectiveMatrix( mat4 m ) {
	return m[ 2 ][ 3 ] == - 1.0;
}
vec2 equirectUv( in vec3 dir ) {
	float u = atan( dir.z, dir.x ) * RECIPROCAL_PI2 + 0.5;
	float v = asin( clamp( dir.y, - 1.0, 1.0 ) ) * RECIPROCAL_PI + 0.5;
	return vec2( u, v );
}
vec3 BRDF_Lambert( const in vec3 diffuseColor ) {
	return RECIPROCAL_PI * diffuseColor;
}
vec3 F_Schlick( const in vec3 f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
}
float F_Schlick( const in float f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
} // validated`,su=`#ifdef ENVMAP_TYPE_CUBE_UV
	#define cubeUV_minMipLevel 4.0
	#define cubeUV_minTileSize 16.0
	float getFace( vec3 direction ) {
		vec3 absDirection = abs( direction );
		float face = - 1.0;
		if ( absDirection.x > absDirection.z ) {
			if ( absDirection.x > absDirection.y )
				face = direction.x > 0.0 ? 0.0 : 3.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		} else {
			if ( absDirection.z > absDirection.y )
				face = direction.z > 0.0 ? 2.0 : 5.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		}
		return face;
	}
	vec2 getUV( vec3 direction, float face ) {
		vec2 uv;
		if ( face == 0.0 ) {
			uv = vec2( direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 1.0 ) {
			uv = vec2( - direction.x, - direction.z ) / abs( direction.y );
		} else if ( face == 2.0 ) {
			uv = vec2( - direction.x, direction.y ) / abs( direction.z );
		} else if ( face == 3.0 ) {
			uv = vec2( - direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 4.0 ) {
			uv = vec2( - direction.x, direction.z ) / abs( direction.y );
		} else {
			uv = vec2( direction.x, direction.y ) / abs( direction.z );
		}
		return 0.5 * ( uv + 1.0 );
	}
	vec3 bilinearCubeUV( sampler2D envMap, vec3 direction, float mipInt ) {
		float face = getFace( direction );
		float filterInt = max( cubeUV_minMipLevel - mipInt, 0.0 );
		mipInt = max( mipInt, cubeUV_minMipLevel );
		float faceSize = exp2( mipInt );
		highp vec2 uv = getUV( direction, face ) * ( faceSize - 2.0 ) + 1.0;
		if ( face > 2.0 ) {
			uv.y += faceSize;
			face -= 3.0;
		}
		uv.x += face * faceSize;
		uv.x += filterInt * 3.0 * cubeUV_minTileSize;
		uv.y += 4.0 * ( exp2( CUBEUV_MAX_MIP ) - faceSize );
		uv.x *= CUBEUV_TEXEL_WIDTH;
		uv.y *= CUBEUV_TEXEL_HEIGHT;
		#ifdef texture2DGradEXT
			return texture2DGradEXT( envMap, uv, vec2( 0.0 ), vec2( 0.0 ) ).rgb;
		#else
			return texture2D( envMap, uv ).rgb;
		#endif
	}
	#define cubeUV_r0 1.0
	#define cubeUV_m0 - 2.0
	#define cubeUV_r1 0.8
	#define cubeUV_m1 - 1.0
	#define cubeUV_r4 0.4
	#define cubeUV_m4 2.0
	#define cubeUV_r5 0.305
	#define cubeUV_m5 3.0
	#define cubeUV_r6 0.21
	#define cubeUV_m6 4.0
	float roughnessToMip( float roughness ) {
		float mip = 0.0;
		if ( roughness >= cubeUV_r1 ) {
			mip = ( cubeUV_r0 - roughness ) * ( cubeUV_m1 - cubeUV_m0 ) / ( cubeUV_r0 - cubeUV_r1 ) + cubeUV_m0;
		} else if ( roughness >= cubeUV_r4 ) {
			mip = ( cubeUV_r1 - roughness ) * ( cubeUV_m4 - cubeUV_m1 ) / ( cubeUV_r1 - cubeUV_r4 ) + cubeUV_m1;
		} else if ( roughness >= cubeUV_r5 ) {
			mip = ( cubeUV_r4 - roughness ) * ( cubeUV_m5 - cubeUV_m4 ) / ( cubeUV_r4 - cubeUV_r5 ) + cubeUV_m4;
		} else if ( roughness >= cubeUV_r6 ) {
			mip = ( cubeUV_r5 - roughness ) * ( cubeUV_m6 - cubeUV_m5 ) / ( cubeUV_r5 - cubeUV_r6 ) + cubeUV_m5;
		} else {
			mip = - 2.0 * log2( 1.16 * roughness );		}
		return mip;
	}
	vec4 textureCubeUV( sampler2D envMap, vec3 sampleDir, float roughness ) {
		float mip = clamp( roughnessToMip( roughness ), cubeUV_m0, CUBEUV_MAX_MIP );
		float mipF = fract( mip );
		float mipInt = floor( mip );
		vec3 color0 = bilinearCubeUV( envMap, sampleDir, mipInt );
		if ( mipF == 0.0 ) {
			return vec4( color0, 1.0 );
		} else {
			vec3 color1 = bilinearCubeUV( envMap, sampleDir, mipInt + 1.0 );
			return vec4( mix( color0, color1, mipF ), 1.0 );
		}
	}
#endif`,au=`vec3 transformedNormal = objectNormal;
#ifdef USE_TANGENT
	vec3 transformedTangent = objectTangent;
#endif
#ifdef USE_BATCHING
	mat3 bm = mat3( batchingMatrix );
	transformedNormal /= vec3( dot( bm[ 0 ], bm[ 0 ] ), dot( bm[ 1 ], bm[ 1 ] ), dot( bm[ 2 ], bm[ 2 ] ) );
	transformedNormal = bm * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = bm * transformedTangent;
	#endif
#endif
#ifdef USE_INSTANCING
	mat3 im = mat3( instanceMatrix );
	transformedNormal /= vec3( dot( im[ 0 ], im[ 0 ] ), dot( im[ 1 ], im[ 1 ] ), dot( im[ 2 ], im[ 2 ] ) );
	transformedNormal = im * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = im * transformedTangent;
	#endif
#endif
transformedNormal = normalMatrix * transformedNormal;
#ifdef FLIP_SIDED
	transformedNormal = - transformedNormal;
#endif
#ifdef USE_TANGENT
	transformedTangent = ( modelViewMatrix * vec4( transformedTangent, 0.0 ) ).xyz;
	#ifdef FLIP_SIDED
		transformedTangent = - transformedTangent;
	#endif
#endif`,ou=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,lu=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,cu=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,uu=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,hu="gl_FragColor = linearToOutputTexel( gl_FragColor );",du=`
const mat3 LINEAR_SRGB_TO_LINEAR_DISPLAY_P3 = mat3(
	vec3( 0.8224621, 0.177538, 0.0 ),
	vec3( 0.0331941, 0.9668058, 0.0 ),
	vec3( 0.0170827, 0.0723974, 0.9105199 )
);
const mat3 LINEAR_DISPLAY_P3_TO_LINEAR_SRGB = mat3(
	vec3( 1.2249401, - 0.2249404, 0.0 ),
	vec3( - 0.0420569, 1.0420571, 0.0 ),
	vec3( - 0.0196376, - 0.0786361, 1.0982735 )
);
vec4 LinearSRGBToLinearDisplayP3( in vec4 value ) {
	return vec4( value.rgb * LINEAR_SRGB_TO_LINEAR_DISPLAY_P3, value.a );
}
vec4 LinearDisplayP3ToLinearSRGB( in vec4 value ) {
	return vec4( value.rgb * LINEAR_DISPLAY_P3_TO_LINEAR_SRGB, value.a );
}
vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,fu=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vec3 cameraToFrag;
		if ( isOrthographic ) {
			cameraToFrag = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToFrag = normalize( vWorldPosition - cameraPosition );
		}
		vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vec3 reflectVec = reflect( cameraToFrag, worldNormal );
		#else
			vec3 reflectVec = refract( cameraToFrag, worldNormal, refractionRatio );
		#endif
	#else
		vec3 reflectVec = vReflect;
	#endif
	#ifdef ENVMAP_TYPE_CUBE
		vec4 envColor = textureCube( envMap, envMapRotation * vec3( flipEnvMap * reflectVec.x, reflectVec.yz ) );
	#else
		vec4 envColor = vec4( 0.0 );
	#endif
	#ifdef ENVMAP_BLENDING_MULTIPLY
		outgoingLight = mix( outgoingLight, outgoingLight * envColor.xyz, specularStrength * reflectivity );
	#elif defined( ENVMAP_BLENDING_MIX )
		outgoingLight = mix( outgoingLight, envColor.xyz, specularStrength * reflectivity );
	#elif defined( ENVMAP_BLENDING_ADD )
		outgoingLight += envColor.xyz * specularStrength * reflectivity;
	#endif
#endif`,pu=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform float flipEnvMap;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
	
#endif`,mu=`#ifdef USE_ENVMAP
	uniform float reflectivity;
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		varying vec3 vWorldPosition;
		uniform float refractionRatio;
	#else
		varying vec3 vReflect;
	#endif
#endif`,_u=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,gu=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vWorldPosition = worldPosition.xyz;
	#else
		vec3 cameraToVertex;
		if ( isOrthographic ) {
			cameraToVertex = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToVertex = normalize( worldPosition.xyz - cameraPosition );
		}
		vec3 worldNormal = inverseTransformDirection( transformedNormal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vReflect = reflect( cameraToVertex, worldNormal );
		#else
			vReflect = refract( cameraToVertex, worldNormal, refractionRatio );
		#endif
	#endif
#endif`,vu=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,xu=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,Mu=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,Su=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,Eu=`#ifdef USE_GRADIENTMAP
	uniform sampler2D gradientMap;
#endif
vec3 getGradientIrradiance( vec3 normal, vec3 lightDirection ) {
	float dotNL = dot( normal, lightDirection );
	vec2 coord = vec2( dotNL * 0.5 + 0.5, 0.0 );
	#ifdef USE_GRADIENTMAP
		return vec3( texture2D( gradientMap, coord ).r );
	#else
		vec2 fw = fwidth( coord ) * 0.5;
		return mix( vec3( 0.7 ), vec3( 1.0 ), smoothstep( 0.7 - fw.x, 0.7 + fw.x, coord.x ) );
	#endif
}`,yu=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,Tu=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,Au=`varying vec3 vViewPosition;
struct LambertMaterial {
	vec3 diffuseColor;
	float specularStrength;
};
void RE_Direct_Lambert( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Lambert( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Lambert
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,bu=`uniform bool receiveShadow;
uniform vec3 ambientLightColor;
#if defined( USE_LIGHT_PROBES )
	uniform vec3 lightProbe[ 9 ];
#endif
vec3 shGetIrradianceAt( in vec3 normal, in vec3 shCoefficients[ 9 ] ) {
	float x = normal.x, y = normal.y, z = normal.z;
	vec3 result = shCoefficients[ 0 ] * 0.886227;
	result += shCoefficients[ 1 ] * 2.0 * 0.511664 * y;
	result += shCoefficients[ 2 ] * 2.0 * 0.511664 * z;
	result += shCoefficients[ 3 ] * 2.0 * 0.511664 * x;
	result += shCoefficients[ 4 ] * 2.0 * 0.429043 * x * y;
	result += shCoefficients[ 5 ] * 2.0 * 0.429043 * y * z;
	result += shCoefficients[ 6 ] * ( 0.743125 * z * z - 0.247708 );
	result += shCoefficients[ 7 ] * 2.0 * 0.429043 * x * z;
	result += shCoefficients[ 8 ] * 0.429043 * ( x * x - y * y );
	return result;
}
vec3 getLightProbeIrradiance( const in vec3 lightProbe[ 9 ], const in vec3 normal ) {
	vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
	vec3 irradiance = shGetIrradianceAt( worldNormal, lightProbe );
	return irradiance;
}
vec3 getAmbientLightIrradiance( const in vec3 ambientLightColor ) {
	vec3 irradiance = ambientLightColor;
	return irradiance;
}
float getDistanceAttenuation( const in float lightDistance, const in float cutoffDistance, const in float decayExponent ) {
	float distanceFalloff = 1.0 / max( pow( lightDistance, decayExponent ), 0.01 );
	if ( cutoffDistance > 0.0 ) {
		distanceFalloff *= pow2( saturate( 1.0 - pow4( lightDistance / cutoffDistance ) ) );
	}
	return distanceFalloff;
}
float getSpotAttenuation( const in float coneCosine, const in float penumbraCosine, const in float angleCosine ) {
	return smoothstep( coneCosine, penumbraCosine, angleCosine );
}
#if NUM_DIR_LIGHTS > 0
	struct DirectionalLight {
		vec3 direction;
		vec3 color;
	};
	uniform DirectionalLight directionalLights[ NUM_DIR_LIGHTS ];
	void getDirectionalLightInfo( const in DirectionalLight directionalLight, out IncidentLight light ) {
		light.color = directionalLight.color;
		light.direction = directionalLight.direction;
		light.visible = true;
	}
#endif
#if NUM_POINT_LIGHTS > 0
	struct PointLight {
		vec3 position;
		vec3 color;
		float distance;
		float decay;
	};
	uniform PointLight pointLights[ NUM_POINT_LIGHTS ];
	void getPointLightInfo( const in PointLight pointLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = pointLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float lightDistance = length( lVector );
		light.color = pointLight.color;
		light.color *= getDistanceAttenuation( lightDistance, pointLight.distance, pointLight.decay );
		light.visible = ( light.color != vec3( 0.0 ) );
	}
#endif
#if NUM_SPOT_LIGHTS > 0
	struct SpotLight {
		vec3 position;
		vec3 direction;
		vec3 color;
		float distance;
		float decay;
		float coneCos;
		float penumbraCos;
	};
	uniform SpotLight spotLights[ NUM_SPOT_LIGHTS ];
	void getSpotLightInfo( const in SpotLight spotLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = spotLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float angleCos = dot( light.direction, spotLight.direction );
		float spotAttenuation = getSpotAttenuation( spotLight.coneCos, spotLight.penumbraCos, angleCos );
		if ( spotAttenuation > 0.0 ) {
			float lightDistance = length( lVector );
			light.color = spotLight.color * spotAttenuation;
			light.color *= getDistanceAttenuation( lightDistance, spotLight.distance, spotLight.decay );
			light.visible = ( light.color != vec3( 0.0 ) );
		} else {
			light.color = vec3( 0.0 );
			light.visible = false;
		}
	}
#endif
#if NUM_RECT_AREA_LIGHTS > 0
	struct RectAreaLight {
		vec3 color;
		vec3 position;
		vec3 halfWidth;
		vec3 halfHeight;
	};
	uniform sampler2D ltc_1;	uniform sampler2D ltc_2;
	uniform RectAreaLight rectAreaLights[ NUM_RECT_AREA_LIGHTS ];
#endif
#if NUM_HEMI_LIGHTS > 0
	struct HemisphereLight {
		vec3 direction;
		vec3 skyColor;
		vec3 groundColor;
	};
	uniform HemisphereLight hemisphereLights[ NUM_HEMI_LIGHTS ];
	vec3 getHemisphereLightIrradiance( const in HemisphereLight hemiLight, const in vec3 normal ) {
		float dotNL = dot( normal, hemiLight.direction );
		float hemiDiffuseWeight = 0.5 * dotNL + 0.5;
		vec3 irradiance = mix( hemiLight.groundColor, hemiLight.skyColor, hemiDiffuseWeight );
		return irradiance;
	}
#endif`,wu=`#ifdef USE_ENVMAP
	vec3 getIBLIrradiance( const in vec3 normal ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * worldNormal, 1.0 );
			return PI * envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	vec3 getIBLRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 reflectVec = reflect( - viewDir, normal );
			reflectVec = normalize( mix( reflectVec, normal, roughness * roughness) );
			reflectVec = inverseTransformDirection( reflectVec, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * reflectVec, roughness );
			return envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	#ifdef USE_ANISOTROPY
		vec3 getIBLAnisotropyRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness, const in vec3 bitangent, const in float anisotropy ) {
			#ifdef ENVMAP_TYPE_CUBE_UV
				vec3 bentNormal = cross( bitangent, viewDir );
				bentNormal = normalize( cross( bentNormal, bitangent ) );
				bentNormal = normalize( mix( bentNormal, normal, pow2( pow2( 1.0 - anisotropy * ( 1.0 - roughness ) ) ) ) );
				return getIBLRadiance( viewDir, bentNormal, roughness );
			#else
				return vec3( 0.0 );
			#endif
		}
	#endif
#endif`,Ru=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,Cu=`varying vec3 vViewPosition;
struct ToonMaterial {
	vec3 diffuseColor;
};
void RE_Direct_Toon( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	vec3 irradiance = getGradientIrradiance( geometryNormal, directLight.direction ) * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Toon( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Toon
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,Pu=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,Lu=`varying vec3 vViewPosition;
struct BlinnPhongMaterial {
	vec3 diffuseColor;
	vec3 specularColor;
	float specularShininess;
	float specularStrength;
};
void RE_Direct_BlinnPhong( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
	reflectedLight.directSpecular += irradiance * BRDF_BlinnPhong( directLight.direction, geometryViewDir, geometryNormal, material.specularColor, material.specularShininess ) * material.specularStrength;
}
void RE_IndirectDiffuse_BlinnPhong( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_BlinnPhong
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,Du=`PhysicalMaterial material;
material.diffuseColor = diffuseColor.rgb * ( 1.0 - metalnessFactor );
vec3 dxy = max( abs( dFdx( nonPerturbedNormal ) ), abs( dFdy( nonPerturbedNormal ) ) );
float geometryRoughness = max( max( dxy.x, dxy.y ), dxy.z );
material.roughness = max( roughnessFactor, 0.0525 );material.roughness += geometryRoughness;
material.roughness = min( material.roughness, 1.0 );
#ifdef IOR
	material.ior = ior;
	#ifdef USE_SPECULAR
		float specularIntensityFactor = specularIntensity;
		vec3 specularColorFactor = specularColor;
		#ifdef USE_SPECULAR_COLORMAP
			specularColorFactor *= texture2D( specularColorMap, vSpecularColorMapUv ).rgb;
		#endif
		#ifdef USE_SPECULAR_INTENSITYMAP
			specularIntensityFactor *= texture2D( specularIntensityMap, vSpecularIntensityMapUv ).a;
		#endif
		material.specularF90 = mix( specularIntensityFactor, 1.0, metalnessFactor );
	#else
		float specularIntensityFactor = 1.0;
		vec3 specularColorFactor = vec3( 1.0 );
		material.specularF90 = 1.0;
	#endif
	material.specularColor = mix( min( pow2( ( material.ior - 1.0 ) / ( material.ior + 1.0 ) ) * specularColorFactor, vec3( 1.0 ) ) * specularIntensityFactor, diffuseColor.rgb, metalnessFactor );
#else
	material.specularColor = mix( vec3( 0.04 ), diffuseColor.rgb, metalnessFactor );
	material.specularF90 = 1.0;
#endif
#ifdef USE_CLEARCOAT
	material.clearcoat = clearcoat;
	material.clearcoatRoughness = clearcoatRoughness;
	material.clearcoatF0 = vec3( 0.04 );
	material.clearcoatF90 = 1.0;
	#ifdef USE_CLEARCOATMAP
		material.clearcoat *= texture2D( clearcoatMap, vClearcoatMapUv ).x;
	#endif
	#ifdef USE_CLEARCOAT_ROUGHNESSMAP
		material.clearcoatRoughness *= texture2D( clearcoatRoughnessMap, vClearcoatRoughnessMapUv ).y;
	#endif
	material.clearcoat = saturate( material.clearcoat );	material.clearcoatRoughness = max( material.clearcoatRoughness, 0.0525 );
	material.clearcoatRoughness += geometryRoughness;
	material.clearcoatRoughness = min( material.clearcoatRoughness, 1.0 );
#endif
#ifdef USE_DISPERSION
	material.dispersion = dispersion;
#endif
#ifdef USE_IRIDESCENCE
	material.iridescence = iridescence;
	material.iridescenceIOR = iridescenceIOR;
	#ifdef USE_IRIDESCENCEMAP
		material.iridescence *= texture2D( iridescenceMap, vIridescenceMapUv ).r;
	#endif
	#ifdef USE_IRIDESCENCE_THICKNESSMAP
		material.iridescenceThickness = (iridescenceThicknessMaximum - iridescenceThicknessMinimum) * texture2D( iridescenceThicknessMap, vIridescenceThicknessMapUv ).g + iridescenceThicknessMinimum;
	#else
		material.iridescenceThickness = iridescenceThicknessMaximum;
	#endif
#endif
#ifdef USE_SHEEN
	material.sheenColor = sheenColor;
	#ifdef USE_SHEEN_COLORMAP
		material.sheenColor *= texture2D( sheenColorMap, vSheenColorMapUv ).rgb;
	#endif
	material.sheenRoughness = clamp( sheenRoughness, 0.07, 1.0 );
	#ifdef USE_SHEEN_ROUGHNESSMAP
		material.sheenRoughness *= texture2D( sheenRoughnessMap, vSheenRoughnessMapUv ).a;
	#endif
#endif
#ifdef USE_ANISOTROPY
	#ifdef USE_ANISOTROPYMAP
		mat2 anisotropyMat = mat2( anisotropyVector.x, anisotropyVector.y, - anisotropyVector.y, anisotropyVector.x );
		vec3 anisotropyPolar = texture2D( anisotropyMap, vAnisotropyMapUv ).rgb;
		vec2 anisotropyV = anisotropyMat * normalize( 2.0 * anisotropyPolar.rg - vec2( 1.0 ) ) * anisotropyPolar.b;
	#else
		vec2 anisotropyV = anisotropyVector;
	#endif
	material.anisotropy = length( anisotropyV );
	if( material.anisotropy == 0.0 ) {
		anisotropyV = vec2( 1.0, 0.0 );
	} else {
		anisotropyV /= material.anisotropy;
		material.anisotropy = saturate( material.anisotropy );
	}
	material.alphaT = mix( pow2( material.roughness ), 1.0, pow2( material.anisotropy ) );
	material.anisotropyT = tbn[ 0 ] * anisotropyV.x + tbn[ 1 ] * anisotropyV.y;
	material.anisotropyB = tbn[ 1 ] * anisotropyV.x - tbn[ 0 ] * anisotropyV.y;
#endif`,Uu=`struct PhysicalMaterial {
	vec3 diffuseColor;
	float roughness;
	vec3 specularColor;
	float specularF90;
	float dispersion;
	#ifdef USE_CLEARCOAT
		float clearcoat;
		float clearcoatRoughness;
		vec3 clearcoatF0;
		float clearcoatF90;
	#endif
	#ifdef USE_IRIDESCENCE
		float iridescence;
		float iridescenceIOR;
		float iridescenceThickness;
		vec3 iridescenceFresnel;
		vec3 iridescenceF0;
	#endif
	#ifdef USE_SHEEN
		vec3 sheenColor;
		float sheenRoughness;
	#endif
	#ifdef IOR
		float ior;
	#endif
	#ifdef USE_TRANSMISSION
		float transmission;
		float transmissionAlpha;
		float thickness;
		float attenuationDistance;
		vec3 attenuationColor;
	#endif
	#ifdef USE_ANISOTROPY
		float anisotropy;
		float alphaT;
		vec3 anisotropyT;
		vec3 anisotropyB;
	#endif
};
vec3 clearcoatSpecularDirect = vec3( 0.0 );
vec3 clearcoatSpecularIndirect = vec3( 0.0 );
vec3 sheenSpecularDirect = vec3( 0.0 );
vec3 sheenSpecularIndirect = vec3(0.0 );
vec3 Schlick_to_F0( const in vec3 f, const in float f90, const in float dotVH ) {
    float x = clamp( 1.0 - dotVH, 0.0, 1.0 );
    float x2 = x * x;
    float x5 = clamp( x * x2 * x2, 0.0, 0.9999 );
    return ( f - vec3( f90 ) * x5 ) / ( 1.0 - x5 );
}
float V_GGX_SmithCorrelated( const in float alpha, const in float dotNL, const in float dotNV ) {
	float a2 = pow2( alpha );
	float gv = dotNL * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNV ) );
	float gl = dotNV * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNL ) );
	return 0.5 / max( gv + gl, EPSILON );
}
float D_GGX( const in float alpha, const in float dotNH ) {
	float a2 = pow2( alpha );
	float denom = pow2( dotNH ) * ( a2 - 1.0 ) + 1.0;
	return RECIPROCAL_PI * a2 / pow2( denom );
}
#ifdef USE_ANISOTROPY
	float V_GGX_SmithCorrelated_Anisotropic( const in float alphaT, const in float alphaB, const in float dotTV, const in float dotBV, const in float dotTL, const in float dotBL, const in float dotNV, const in float dotNL ) {
		float gv = dotNL * length( vec3( alphaT * dotTV, alphaB * dotBV, dotNV ) );
		float gl = dotNV * length( vec3( alphaT * dotTL, alphaB * dotBL, dotNL ) );
		float v = 0.5 / ( gv + gl );
		return saturate(v);
	}
	float D_GGX_Anisotropic( const in float alphaT, const in float alphaB, const in float dotNH, const in float dotTH, const in float dotBH ) {
		float a2 = alphaT * alphaB;
		highp vec3 v = vec3( alphaB * dotTH, alphaT * dotBH, a2 * dotNH );
		highp float v2 = dot( v, v );
		float w2 = a2 / v2;
		return RECIPROCAL_PI * a2 * pow2 ( w2 );
	}
#endif
#ifdef USE_CLEARCOAT
	vec3 BRDF_GGX_Clearcoat( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material) {
		vec3 f0 = material.clearcoatF0;
		float f90 = material.clearcoatF90;
		float roughness = material.clearcoatRoughness;
		float alpha = pow2( roughness );
		vec3 halfDir = normalize( lightDir + viewDir );
		float dotNL = saturate( dot( normal, lightDir ) );
		float dotNV = saturate( dot( normal, viewDir ) );
		float dotNH = saturate( dot( normal, halfDir ) );
		float dotVH = saturate( dot( viewDir, halfDir ) );
		vec3 F = F_Schlick( f0, f90, dotVH );
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
		return F * ( V * D );
	}
#endif
vec3 BRDF_GGX( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material ) {
	vec3 f0 = material.specularColor;
	float f90 = material.specularF90;
	float roughness = material.roughness;
	float alpha = pow2( roughness );
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( f0, f90, dotVH );
	#ifdef USE_IRIDESCENCE
		F = mix( F, material.iridescenceFresnel, material.iridescence );
	#endif
	#ifdef USE_ANISOTROPY
		float dotTL = dot( material.anisotropyT, lightDir );
		float dotTV = dot( material.anisotropyT, viewDir );
		float dotTH = dot( material.anisotropyT, halfDir );
		float dotBL = dot( material.anisotropyB, lightDir );
		float dotBV = dot( material.anisotropyB, viewDir );
		float dotBH = dot( material.anisotropyB, halfDir );
		float V = V_GGX_SmithCorrelated_Anisotropic( material.alphaT, alpha, dotTV, dotBV, dotTL, dotBL, dotNV, dotNL );
		float D = D_GGX_Anisotropic( material.alphaT, alpha, dotNH, dotTH, dotBH );
	#else
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
	#endif
	return F * ( V * D );
}
vec2 LTC_Uv( const in vec3 N, const in vec3 V, const in float roughness ) {
	const float LUT_SIZE = 64.0;
	const float LUT_SCALE = ( LUT_SIZE - 1.0 ) / LUT_SIZE;
	const float LUT_BIAS = 0.5 / LUT_SIZE;
	float dotNV = saturate( dot( N, V ) );
	vec2 uv = vec2( roughness, sqrt( 1.0 - dotNV ) );
	uv = uv * LUT_SCALE + LUT_BIAS;
	return uv;
}
float LTC_ClippedSphereFormFactor( const in vec3 f ) {
	float l = length( f );
	return max( ( l * l + f.z ) / ( l + 1.0 ), 0.0 );
}
vec3 LTC_EdgeVectorFormFactor( const in vec3 v1, const in vec3 v2 ) {
	float x = dot( v1, v2 );
	float y = abs( x );
	float a = 0.8543985 + ( 0.4965155 + 0.0145206 * y ) * y;
	float b = 3.4175940 + ( 4.1616724 + y ) * y;
	float v = a / b;
	float theta_sintheta = ( x > 0.0 ) ? v : 0.5 * inversesqrt( max( 1.0 - x * x, 1e-7 ) ) - v;
	return cross( v1, v2 ) * theta_sintheta;
}
vec3 LTC_Evaluate( const in vec3 N, const in vec3 V, const in vec3 P, const in mat3 mInv, const in vec3 rectCoords[ 4 ] ) {
	vec3 v1 = rectCoords[ 1 ] - rectCoords[ 0 ];
	vec3 v2 = rectCoords[ 3 ] - rectCoords[ 0 ];
	vec3 lightNormal = cross( v1, v2 );
	if( dot( lightNormal, P - rectCoords[ 0 ] ) < 0.0 ) return vec3( 0.0 );
	vec3 T1, T2;
	T1 = normalize( V - N * dot( V, N ) );
	T2 = - cross( N, T1 );
	mat3 mat = mInv * transposeMat3( mat3( T1, T2, N ) );
	vec3 coords[ 4 ];
	coords[ 0 ] = mat * ( rectCoords[ 0 ] - P );
	coords[ 1 ] = mat * ( rectCoords[ 1 ] - P );
	coords[ 2 ] = mat * ( rectCoords[ 2 ] - P );
	coords[ 3 ] = mat * ( rectCoords[ 3 ] - P );
	coords[ 0 ] = normalize( coords[ 0 ] );
	coords[ 1 ] = normalize( coords[ 1 ] );
	coords[ 2 ] = normalize( coords[ 2 ] );
	coords[ 3 ] = normalize( coords[ 3 ] );
	vec3 vectorFormFactor = vec3( 0.0 );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 0 ], coords[ 1 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 1 ], coords[ 2 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 2 ], coords[ 3 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 3 ], coords[ 0 ] );
	float result = LTC_ClippedSphereFormFactor( vectorFormFactor );
	return vec3( result );
}
#if defined( USE_SHEEN )
float D_Charlie( float roughness, float dotNH ) {
	float alpha = pow2( roughness );
	float invAlpha = 1.0 / alpha;
	float cos2h = dotNH * dotNH;
	float sin2h = max( 1.0 - cos2h, 0.0078125 );
	return ( 2.0 + invAlpha ) * pow( sin2h, invAlpha * 0.5 ) / ( 2.0 * PI );
}
float V_Neubelt( float dotNV, float dotNL ) {
	return saturate( 1.0 / ( 4.0 * ( dotNL + dotNV - dotNL * dotNV ) ) );
}
vec3 BRDF_Sheen( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, vec3 sheenColor, const in float sheenRoughness ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float D = D_Charlie( sheenRoughness, dotNH );
	float V = V_Neubelt( dotNV, dotNL );
	return sheenColor * ( D * V );
}
#endif
float IBLSheenBRDF( const in vec3 normal, const in vec3 viewDir, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	float r2 = roughness * roughness;
	float a = roughness < 0.25 ? -339.2 * r2 + 161.4 * roughness - 25.9 : -8.48 * r2 + 14.3 * roughness - 9.95;
	float b = roughness < 0.25 ? 44.0 * r2 - 23.7 * roughness + 3.26 : 1.97 * r2 - 3.27 * roughness + 0.72;
	float DG = exp( a * dotNV + b ) + ( roughness < 0.25 ? 0.0 : 0.1 * ( roughness - 0.25 ) );
	return saturate( DG * RECIPROCAL_PI );
}
vec2 DFGApprox( const in vec3 normal, const in vec3 viewDir, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	const vec4 c0 = vec4( - 1, - 0.0275, - 0.572, 0.022 );
	const vec4 c1 = vec4( 1, 0.0425, 1.04, - 0.04 );
	vec4 r = roughness * c0 + c1;
	float a004 = min( r.x * r.x, exp2( - 9.28 * dotNV ) ) * r.x + r.y;
	vec2 fab = vec2( - 1.04, 1.04 ) * a004 + r.zw;
	return fab;
}
vec3 EnvironmentBRDF( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness ) {
	vec2 fab = DFGApprox( normal, viewDir, roughness );
	return specularColor * fab.x + specularF90 * fab.y;
}
#ifdef USE_IRIDESCENCE
void computeMultiscatteringIridescence( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float iridescence, const in vec3 iridescenceF0, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#else
void computeMultiscattering( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#endif
	vec2 fab = DFGApprox( normal, viewDir, roughness );
	#ifdef USE_IRIDESCENCE
		vec3 Fr = mix( specularColor, iridescenceF0, iridescence );
	#else
		vec3 Fr = specularColor;
	#endif
	vec3 FssEss = Fr * fab.x + specularF90 * fab.y;
	float Ess = fab.x + fab.y;
	float Ems = 1.0 - Ess;
	vec3 Favg = Fr + ( 1.0 - Fr ) * 0.047619;	vec3 Fms = FssEss * Favg / ( 1.0 - Ems * Favg );
	singleScatter += FssEss;
	multiScatter += Fms * Ems;
}
#if NUM_RECT_AREA_LIGHTS > 0
	void RE_Direct_RectArea_Physical( const in RectAreaLight rectAreaLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
		vec3 normal = geometryNormal;
		vec3 viewDir = geometryViewDir;
		vec3 position = geometryPosition;
		vec3 lightPos = rectAreaLight.position;
		vec3 halfWidth = rectAreaLight.halfWidth;
		vec3 halfHeight = rectAreaLight.halfHeight;
		vec3 lightColor = rectAreaLight.color;
		float roughness = material.roughness;
		vec3 rectCoords[ 4 ];
		rectCoords[ 0 ] = lightPos + halfWidth - halfHeight;		rectCoords[ 1 ] = lightPos - halfWidth - halfHeight;
		rectCoords[ 2 ] = lightPos - halfWidth + halfHeight;
		rectCoords[ 3 ] = lightPos + halfWidth + halfHeight;
		vec2 uv = LTC_Uv( normal, viewDir, roughness );
		vec4 t1 = texture2D( ltc_1, uv );
		vec4 t2 = texture2D( ltc_2, uv );
		mat3 mInv = mat3(
			vec3( t1.x, 0, t1.y ),
			vec3(    0, 1,    0 ),
			vec3( t1.z, 0, t1.w )
		);
		vec3 fresnel = ( material.specularColor * t2.x + ( vec3( 1.0 ) - material.specularColor ) * t2.y );
		reflectedLight.directSpecular += lightColor * fresnel * LTC_Evaluate( normal, viewDir, position, mInv, rectCoords );
		reflectedLight.directDiffuse += lightColor * material.diffuseColor * LTC_Evaluate( normal, viewDir, position, mat3( 1.0 ), rectCoords );
	}
#endif
void RE_Direct_Physical( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	#ifdef USE_CLEARCOAT
		float dotNLcc = saturate( dot( geometryClearcoatNormal, directLight.direction ) );
		vec3 ccIrradiance = dotNLcc * directLight.color;
		clearcoatSpecularDirect += ccIrradiance * BRDF_GGX_Clearcoat( directLight.direction, geometryViewDir, geometryClearcoatNormal, material );
	#endif
	#ifdef USE_SHEEN
		sheenSpecularDirect += irradiance * BRDF_Sheen( directLight.direction, geometryViewDir, geometryNormal, material.sheenColor, material.sheenRoughness );
	#endif
	reflectedLight.directSpecular += irradiance * BRDF_GGX( directLight.direction, geometryViewDir, geometryNormal, material );
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Physical( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectSpecular_Physical( const in vec3 radiance, const in vec3 irradiance, const in vec3 clearcoatRadiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight) {
	#ifdef USE_CLEARCOAT
		clearcoatSpecularIndirect += clearcoatRadiance * EnvironmentBRDF( geometryClearcoatNormal, geometryViewDir, material.clearcoatF0, material.clearcoatF90, material.clearcoatRoughness );
	#endif
	#ifdef USE_SHEEN
		sheenSpecularIndirect += irradiance * material.sheenColor * IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
	#endif
	vec3 singleScattering = vec3( 0.0 );
	vec3 multiScattering = vec3( 0.0 );
	vec3 cosineWeightedIrradiance = irradiance * RECIPROCAL_PI;
	#ifdef USE_IRIDESCENCE
		computeMultiscatteringIridescence( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.iridescence, material.iridescenceFresnel, material.roughness, singleScattering, multiScattering );
	#else
		computeMultiscattering( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.roughness, singleScattering, multiScattering );
	#endif
	vec3 totalScattering = singleScattering + multiScattering;
	vec3 diffuse = material.diffuseColor * ( 1.0 - max( max( totalScattering.r, totalScattering.g ), totalScattering.b ) );
	reflectedLight.indirectSpecular += radiance * singleScattering;
	reflectedLight.indirectSpecular += multiScattering * cosineWeightedIrradiance;
	reflectedLight.indirectDiffuse += diffuse * cosineWeightedIrradiance;
}
#define RE_Direct				RE_Direct_Physical
#define RE_Direct_RectArea		RE_Direct_RectArea_Physical
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Physical
#define RE_IndirectSpecular		RE_IndirectSpecular_Physical
float computeSpecularOcclusion( const in float dotNV, const in float ambientOcclusion, const in float roughness ) {
	return saturate( pow( dotNV + ambientOcclusion, exp2( - 16.0 * roughness - 1.0 ) ) - 1.0 + ambientOcclusion );
}`,Iu=`
vec3 geometryPosition = - vViewPosition;
vec3 geometryNormal = normal;
vec3 geometryViewDir = ( isOrthographic ) ? vec3( 0, 0, 1 ) : normalize( vViewPosition );
vec3 geometryClearcoatNormal = vec3( 0.0 );
#ifdef USE_CLEARCOAT
	geometryClearcoatNormal = clearcoatNormal;
#endif
#ifdef USE_IRIDESCENCE
	float dotNVi = saturate( dot( normal, geometryViewDir ) );
	if ( material.iridescenceThickness == 0.0 ) {
		material.iridescence = 0.0;
	} else {
		material.iridescence = saturate( material.iridescence );
	}
	if ( material.iridescence > 0.0 ) {
		material.iridescenceFresnel = evalIridescence( 1.0, material.iridescenceIOR, dotNVi, material.iridescenceThickness, material.specularColor );
		material.iridescenceF0 = Schlick_to_F0( material.iridescenceFresnel, 1.0, dotNVi );
	}
#endif
IncidentLight directLight;
#if ( NUM_POINT_LIGHTS > 0 ) && defined( RE_Direct )
	PointLight pointLight;
	#if defined( USE_SHADOWMAP ) && NUM_POINT_LIGHT_SHADOWS > 0
	PointLightShadow pointLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHTS; i ++ ) {
		pointLight = pointLights[ i ];
		getPointLightInfo( pointLight, geometryPosition, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_POINT_LIGHT_SHADOWS )
		pointLightShadow = pointLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getPointShadow( pointShadowMap[ i ], pointLightShadow.shadowMapSize, pointLightShadow.shadowIntensity, pointLightShadow.shadowBias, pointLightShadow.shadowRadius, vPointShadowCoord[ i ], pointLightShadow.shadowCameraNear, pointLightShadow.shadowCameraFar ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_SPOT_LIGHTS > 0 ) && defined( RE_Direct )
	SpotLight spotLight;
	vec4 spotColor;
	vec3 spotLightCoord;
	bool inSpotLightMap;
	#if defined( USE_SHADOWMAP ) && NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHTS; i ++ ) {
		spotLight = spotLights[ i ];
		getSpotLightInfo( spotLight, geometryPosition, directLight );
		#if ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#define SPOT_LIGHT_MAP_INDEX UNROLLED_LOOP_INDEX
		#elif ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		#define SPOT_LIGHT_MAP_INDEX NUM_SPOT_LIGHT_MAPS
		#else
		#define SPOT_LIGHT_MAP_INDEX ( UNROLLED_LOOP_INDEX - NUM_SPOT_LIGHT_SHADOWS + NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#endif
		#if ( SPOT_LIGHT_MAP_INDEX < NUM_SPOT_LIGHT_MAPS )
			spotLightCoord = vSpotLightCoord[ i ].xyz / vSpotLightCoord[ i ].w;
			inSpotLightMap = all( lessThan( abs( spotLightCoord * 2. - 1. ), vec3( 1.0 ) ) );
			spotColor = texture2D( spotLightMap[ SPOT_LIGHT_MAP_INDEX ], spotLightCoord.xy );
			directLight.color = inSpotLightMap ? directLight.color * spotColor.rgb : directLight.color;
		#endif
		#undef SPOT_LIGHT_MAP_INDEX
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		spotLightShadow = spotLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( spotShadowMap[ i ], spotLightShadow.shadowMapSize, spotLightShadow.shadowIntensity, spotLightShadow.shadowBias, spotLightShadow.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_DIR_LIGHTS > 0 ) && defined( RE_Direct )
	DirectionalLight directionalLight;
	#if defined( USE_SHADOWMAP ) && NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHTS; i ++ ) {
		directionalLight = directionalLights[ i ];
		getDirectionalLightInfo( directionalLight, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_DIR_LIGHT_SHADOWS )
		directionalLightShadow = directionalLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( directionalShadowMap[ i ], directionalLightShadow.shadowMapSize, directionalLightShadow.shadowIntensity, directionalLightShadow.shadowBias, directionalLightShadow.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_RECT_AREA_LIGHTS > 0 ) && defined( RE_Direct_RectArea )
	RectAreaLight rectAreaLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_RECT_AREA_LIGHTS; i ++ ) {
		rectAreaLight = rectAreaLights[ i ];
		RE_Direct_RectArea( rectAreaLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if defined( RE_IndirectDiffuse )
	vec3 iblIrradiance = vec3( 0.0 );
	vec3 irradiance = getAmbientLightIrradiance( ambientLightColor );
	#if defined( USE_LIGHT_PROBES )
		irradiance += getLightProbeIrradiance( lightProbe, geometryNormal );
	#endif
	#if ( NUM_HEMI_LIGHTS > 0 )
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_HEMI_LIGHTS; i ++ ) {
			irradiance += getHemisphereLightIrradiance( hemisphereLights[ i ], geometryNormal );
		}
		#pragma unroll_loop_end
	#endif
#endif
#if defined( RE_IndirectSpecular )
	vec3 radiance = vec3( 0.0 );
	vec3 clearcoatRadiance = vec3( 0.0 );
#endif`,Nu=`#if defined( RE_IndirectDiffuse )
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		vec3 lightMapIrradiance = lightMapTexel.rgb * lightMapIntensity;
		irradiance += lightMapIrradiance;
	#endif
	#if defined( USE_ENVMAP ) && defined( STANDARD ) && defined( ENVMAP_TYPE_CUBE_UV )
		iblIrradiance += getIBLIrradiance( geometryNormal );
	#endif
#endif
#if defined( USE_ENVMAP ) && defined( RE_IndirectSpecular )
	#ifdef USE_ANISOTROPY
		radiance += getIBLAnisotropyRadiance( geometryViewDir, geometryNormal, material.roughness, material.anisotropyB, material.anisotropy );
	#else
		radiance += getIBLRadiance( geometryViewDir, geometryNormal, material.roughness );
	#endif
	#ifdef USE_CLEARCOAT
		clearcoatRadiance += getIBLRadiance( geometryViewDir, geometryClearcoatNormal, material.clearcoatRoughness );
	#endif
#endif`,Fu=`#if defined( RE_IndirectDiffuse )
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,Ou=`#if defined( USE_LOGDEPTHBUF )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,Bu=`#if defined( USE_LOGDEPTHBUF )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,zu=`#ifdef USE_LOGDEPTHBUF
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,Hu=`#ifdef USE_LOGDEPTHBUF
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,Gu=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = vec4( mix( pow( sampledDiffuseColor.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), sampledDiffuseColor.rgb * 0.0773993808, vec3( lessThanEqual( sampledDiffuseColor.rgb, vec3( 0.04045 ) ) ) ), sampledDiffuseColor.w );
	
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,Vu=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,ku=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
	#if defined( USE_POINTS_UV )
		vec2 uv = vUv;
	#else
		vec2 uv = ( uvTransform * vec3( gl_PointCoord.x, 1.0 - gl_PointCoord.y, 1 ) ).xy;
	#endif
#endif
#ifdef USE_MAP
	diffuseColor *= texture2D( map, uv );
#endif
#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, uv ).g;
#endif`,Wu=`#if defined( USE_POINTS_UV )
	varying vec2 vUv;
#else
	#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
		uniform mat3 uvTransform;
	#endif
#endif
#ifdef USE_MAP
	uniform sampler2D map;
#endif
#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,Xu=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,qu=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,Yu=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,Ku=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,$u=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,Zu=`#ifdef USE_MORPHTARGETS
	#ifndef USE_INSTANCING_MORPH
		uniform float morphTargetBaseInfluence;
		uniform float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	#endif
	uniform sampler2DArray morphTargetsTexture;
	uniform ivec2 morphTargetsTextureSize;
	vec4 getMorph( const in int vertexIndex, const in int morphTargetIndex, const in int offset ) {
		int texelIndex = vertexIndex * MORPHTARGETS_TEXTURE_STRIDE + offset;
		int y = texelIndex / morphTargetsTextureSize.x;
		int x = texelIndex - y * morphTargetsTextureSize.x;
		ivec3 morphUV = ivec3( x, y, morphTargetIndex );
		return texelFetch( morphTargetsTexture, morphUV, 0 );
	}
#endif`,ju=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,Ju=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
#ifdef FLAT_SHADED
	vec3 fdx = dFdx( vViewPosition );
	vec3 fdy = dFdy( vViewPosition );
	vec3 normal = normalize( cross( fdx, fdy ) );
#else
	vec3 normal = normalize( vNormal );
	#ifdef DOUBLE_SIDED
		normal *= faceDirection;
	#endif
#endif
#if defined( USE_NORMALMAP_TANGENTSPACE ) || defined( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY )
	#ifdef USE_TANGENT
		mat3 tbn = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn = getTangentFrame( - vViewPosition, normal,
		#if defined( USE_NORMALMAP )
			vNormalMapUv
		#elif defined( USE_CLEARCOAT_NORMALMAP )
			vClearcoatNormalMapUv
		#else
			vUv
		#endif
		);
	#endif
	#if defined( DOUBLE_SIDED ) && ! defined( FLAT_SHADED )
		tbn[0] *= faceDirection;
		tbn[1] *= faceDirection;
	#endif
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	#ifdef USE_TANGENT
		mat3 tbn2 = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn2 = getTangentFrame( - vViewPosition, normal, vClearcoatNormalMapUv );
	#endif
	#if defined( DOUBLE_SIDED ) && ! defined( FLAT_SHADED )
		tbn2[0] *= faceDirection;
		tbn2[1] *= faceDirection;
	#endif
#endif
vec3 nonPerturbedNormal = normal;`,Qu=`#ifdef USE_NORMALMAP_OBJECTSPACE
	normal = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	#ifdef FLIP_SIDED
		normal = - normal;
	#endif
	#ifdef DOUBLE_SIDED
		normal = normal * faceDirection;
	#endif
	normal = normalize( normalMatrix * normal );
#elif defined( USE_NORMALMAP_TANGENTSPACE )
	vec3 mapN = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	mapN.xy *= normalScale;
	normal = normalize( tbn * mapN );
#elif defined( USE_BUMPMAP )
	normal = perturbNormalArb( - vViewPosition, normal, dHdxy_fwd(), faceDirection );
#endif`,eh=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,th=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,nh=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
	#endif
#endif`,ih=`#ifdef USE_NORMALMAP
	uniform sampler2D normalMap;
	uniform vec2 normalScale;
#endif
#ifdef USE_NORMALMAP_OBJECTSPACE
	uniform mat3 normalMatrix;
#endif
#if ! defined ( USE_TANGENT ) && ( defined ( USE_NORMALMAP_TANGENTSPACE ) || defined ( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY ) )
	mat3 getTangentFrame( vec3 eye_pos, vec3 surf_norm, vec2 uv ) {
		vec3 q0 = dFdx( eye_pos.xyz );
		vec3 q1 = dFdy( eye_pos.xyz );
		vec2 st0 = dFdx( uv.st );
		vec2 st1 = dFdy( uv.st );
		vec3 N = surf_norm;
		vec3 q1perp = cross( q1, N );
		vec3 q0perp = cross( N, q0 );
		vec3 T = q1perp * st0.x + q0perp * st1.x;
		vec3 B = q1perp * st0.y + q0perp * st1.y;
		float det = max( dot( T, T ), dot( B, B ) );
		float scale = ( det == 0.0 ) ? 0.0 : inversesqrt( det );
		return mat3( T * scale, B * scale, N );
	}
#endif`,rh=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,sh=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,ah=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,oh=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,lh=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,ch=`vec3 packNormalToRGB( const in vec3 normal ) {
	return normalize( normal ) * 0.5 + 0.5;
}
vec3 unpackRGBToNormal( const in vec3 rgb ) {
	return 2.0 * rgb.xyz - 1.0;
}
const float PackUpscale = 256. / 255.;const float UnpackDownscale = 255. / 256.;const float ShiftRight8 = 1. / 256.;
const float Inv255 = 1. / 255.;
const vec4 PackFactors = vec4( 1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0 );
const vec2 UnpackFactors2 = vec2( UnpackDownscale, 1.0 / PackFactors.g );
const vec3 UnpackFactors3 = vec3( UnpackDownscale / PackFactors.rg, 1.0 / PackFactors.b );
const vec4 UnpackFactors4 = vec4( UnpackDownscale / PackFactors.rgb, 1.0 / PackFactors.a );
vec4 packDepthToRGBA( const in float v ) {
	if( v <= 0.0 )
		return vec4( 0., 0., 0., 0. );
	if( v >= 1.0 )
		return vec4( 1., 1., 1., 1. );
	float vuf;
	float af = modf( v * PackFactors.a, vuf );
	float bf = modf( vuf * ShiftRight8, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec4( vuf * Inv255, gf * PackUpscale, bf * PackUpscale, af );
}
vec3 packDepthToRGB( const in float v ) {
	if( v <= 0.0 )
		return vec3( 0., 0., 0. );
	if( v >= 1.0 )
		return vec3( 1., 1., 1. );
	float vuf;
	float bf = modf( v * PackFactors.b, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec3( vuf * Inv255, gf * PackUpscale, bf );
}
vec2 packDepthToRG( const in float v ) {
	if( v <= 0.0 )
		return vec2( 0., 0. );
	if( v >= 1.0 )
		return vec2( 1., 1. );
	float vuf;
	float gf = modf( v * 256., vuf );
	return vec2( vuf * Inv255, gf );
}
float unpackRGBAToDepth( const in vec4 v ) {
	return dot( v, UnpackFactors4 );
}
float unpackRGBToDepth( const in vec3 v ) {
	return dot( v, UnpackFactors3 );
}
float unpackRGToDepth( const in vec2 v ) {
	return v.r * UnpackFactors2.r + v.g * UnpackFactors2.g;
}
vec4 pack2HalfToRGBA( const in vec2 v ) {
	vec4 r = vec4( v.x, fract( v.x * 255.0 ), v.y, fract( v.y * 255.0 ) );
	return vec4( r.x - r.y / 255.0, r.y, r.z - r.w / 255.0, r.w );
}
vec2 unpackRGBATo2Half( const in vec4 v ) {
	return vec2( v.x + ( v.y / 255.0 ), v.z + ( v.w / 255.0 ) );
}
float viewZToOrthographicDepth( const in float viewZ, const in float near, const in float far ) {
	return ( viewZ + near ) / ( near - far );
}
float orthographicDepthToViewZ( const in float depth, const in float near, const in float far ) {
	return depth * ( near - far ) - near;
}
float viewZToPerspectiveDepth( const in float viewZ, const in float near, const in float far ) {
	return ( ( near + viewZ ) * far ) / ( ( far - near ) * viewZ );
}
float perspectiveDepthToViewZ( const in float depth, const in float near, const in float far ) {
	return ( near * far ) / ( ( far - near ) * depth - far );
}`,uh=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,hh=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,dh=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,fh=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,ph=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,mh=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,_h=`#if NUM_SPOT_LIGHT_COORDS > 0
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#if NUM_SPOT_LIGHT_MAPS > 0
	uniform sampler2D spotLightMap[ NUM_SPOT_LIGHT_MAPS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		uniform sampler2D directionalShadowMap[ NUM_DIR_LIGHT_SHADOWS ];
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		uniform sampler2D spotShadowMap[ NUM_SPOT_LIGHT_SHADOWS ];
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		uniform sampler2D pointShadowMap[ NUM_POINT_LIGHT_SHADOWS ];
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
	float texture2DCompare( sampler2D depths, vec2 uv, float compare ) {
		return step( compare, unpackRGBAToDepth( texture2D( depths, uv ) ) );
	}
	vec2 texture2DDistribution( sampler2D shadow, vec2 uv ) {
		return unpackRGBATo2Half( texture2D( shadow, uv ) );
	}
	float VSMShadow (sampler2D shadow, vec2 uv, float compare ){
		float occlusion = 1.0;
		vec2 distribution = texture2DDistribution( shadow, uv );
		float hard_shadow = step( compare , distribution.x );
		if (hard_shadow != 1.0 ) {
			float distance = compare - distribution.x ;
			float variance = max( 0.00000, distribution.y * distribution.y );
			float softness_probability = variance / (variance + distance * distance );			softness_probability = clamp( ( softness_probability - 0.3 ) / ( 0.95 - 0.3 ), 0.0, 1.0 );			occlusion = clamp( max( hard_shadow, softness_probability ), 0.0, 1.0 );
		}
		return occlusion;
	}
	float getShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
		float shadow = 1.0;
		shadowCoord.xyz /= shadowCoord.w;
		shadowCoord.z += shadowBias;
		bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
		bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
		if ( frustumTest ) {
		#if defined( SHADOWMAP_TYPE_PCF )
			vec2 texelSize = vec2( 1.0 ) / shadowMapSize;
			float dx0 = - texelSize.x * shadowRadius;
			float dy0 = - texelSize.y * shadowRadius;
			float dx1 = + texelSize.x * shadowRadius;
			float dy1 = + texelSize.y * shadowRadius;
			float dx2 = dx0 / 2.0;
			float dy2 = dy0 / 2.0;
			float dx3 = dx1 / 2.0;
			float dy3 = dy1 / 2.0;
			shadow = (
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx0, dy0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( 0.0, dy0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx1, dy0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx2, dy2 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( 0.0, dy2 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx3, dy2 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx0, 0.0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx2, 0.0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy, shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx3, 0.0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx1, 0.0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx2, dy3 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( 0.0, dy3 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx3, dy3 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx0, dy1 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( 0.0, dy1 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, shadowCoord.xy + vec2( dx1, dy1 ), shadowCoord.z )
			) * ( 1.0 / 17.0 );
		#elif defined( SHADOWMAP_TYPE_PCF_SOFT )
			vec2 texelSize = vec2( 1.0 ) / shadowMapSize;
			float dx = texelSize.x;
			float dy = texelSize.y;
			vec2 uv = shadowCoord.xy;
			vec2 f = fract( uv * shadowMapSize + 0.5 );
			uv -= f * texelSize;
			shadow = (
				texture2DCompare( shadowMap, uv, shadowCoord.z ) +
				texture2DCompare( shadowMap, uv + vec2( dx, 0.0 ), shadowCoord.z ) +
				texture2DCompare( shadowMap, uv + vec2( 0.0, dy ), shadowCoord.z ) +
				texture2DCompare( shadowMap, uv + texelSize, shadowCoord.z ) +
				mix( texture2DCompare( shadowMap, uv + vec2( -dx, 0.0 ), shadowCoord.z ),
					 texture2DCompare( shadowMap, uv + vec2( 2.0 * dx, 0.0 ), shadowCoord.z ),
					 f.x ) +
				mix( texture2DCompare( shadowMap, uv + vec2( -dx, dy ), shadowCoord.z ),
					 texture2DCompare( shadowMap, uv + vec2( 2.0 * dx, dy ), shadowCoord.z ),
					 f.x ) +
				mix( texture2DCompare( shadowMap, uv + vec2( 0.0, -dy ), shadowCoord.z ),
					 texture2DCompare( shadowMap, uv + vec2( 0.0, 2.0 * dy ), shadowCoord.z ),
					 f.y ) +
				mix( texture2DCompare( shadowMap, uv + vec2( dx, -dy ), shadowCoord.z ),
					 texture2DCompare( shadowMap, uv + vec2( dx, 2.0 * dy ), shadowCoord.z ),
					 f.y ) +
				mix( mix( texture2DCompare( shadowMap, uv + vec2( -dx, -dy ), shadowCoord.z ),
						  texture2DCompare( shadowMap, uv + vec2( 2.0 * dx, -dy ), shadowCoord.z ),
						  f.x ),
					 mix( texture2DCompare( shadowMap, uv + vec2( -dx, 2.0 * dy ), shadowCoord.z ),
						  texture2DCompare( shadowMap, uv + vec2( 2.0 * dx, 2.0 * dy ), shadowCoord.z ),
						  f.x ),
					 f.y )
			) * ( 1.0 / 9.0 );
		#elif defined( SHADOWMAP_TYPE_VSM )
			shadow = VSMShadow( shadowMap, shadowCoord.xy, shadowCoord.z );
		#else
			shadow = texture2DCompare( shadowMap, shadowCoord.xy, shadowCoord.z );
		#endif
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
	vec2 cubeToUV( vec3 v, float texelSizeY ) {
		vec3 absV = abs( v );
		float scaleToCube = 1.0 / max( absV.x, max( absV.y, absV.z ) );
		absV *= scaleToCube;
		v *= scaleToCube * ( 1.0 - 2.0 * texelSizeY );
		vec2 planar = v.xy;
		float almostATexel = 1.5 * texelSizeY;
		float almostOne = 1.0 - almostATexel;
		if ( absV.z >= almostOne ) {
			if ( v.z > 0.0 )
				planar.x = 4.0 - v.x;
		} else if ( absV.x >= almostOne ) {
			float signX = sign( v.x );
			planar.x = v.z * signX + 2.0 * signX;
		} else if ( absV.y >= almostOne ) {
			float signY = sign( v.y );
			planar.x = v.x + 2.0 * signY + 2.0;
			planar.y = v.z * signY - 2.0;
		}
		return vec2( 0.125, 0.25 ) * planar + vec2( 0.375, 0.75 );
	}
	float getPointShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord, float shadowCameraNear, float shadowCameraFar ) {
		float shadow = 1.0;
		vec3 lightToPosition = shadowCoord.xyz;
		
		float lightToPositionLength = length( lightToPosition );
		if ( lightToPositionLength - shadowCameraFar <= 0.0 && lightToPositionLength - shadowCameraNear >= 0.0 ) {
			float dp = ( lightToPositionLength - shadowCameraNear ) / ( shadowCameraFar - shadowCameraNear );			dp += shadowBias;
			vec3 bd3D = normalize( lightToPosition );
			vec2 texelSize = vec2( 1.0 ) / ( shadowMapSize * vec2( 4.0, 2.0 ) );
			#if defined( SHADOWMAP_TYPE_PCF ) || defined( SHADOWMAP_TYPE_PCF_SOFT ) || defined( SHADOWMAP_TYPE_VSM )
				vec2 offset = vec2( - 1, 1 ) * shadowRadius * texelSize.y;
				shadow = (
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.xyy, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.yyy, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.xyx, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.yyx, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.xxy, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.yxy, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.xxx, texelSize.y ), dp ) +
					texture2DCompare( shadowMap, cubeToUV( bd3D + offset.yxx, texelSize.y ), dp )
				) * ( 1.0 / 9.0 );
			#else
				shadow = texture2DCompare( shadowMap, cubeToUV( bd3D, texelSize.y ), dp );
			#endif
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
#endif`,gh=`#if NUM_SPOT_LIGHT_COORDS > 0
	uniform mat4 spotLightMatrix[ NUM_SPOT_LIGHT_COORDS ];
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		uniform mat4 directionalShadowMatrix[ NUM_DIR_LIGHT_SHADOWS ];
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		uniform mat4 pointShadowMatrix[ NUM_POINT_LIGHT_SHADOWS ];
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
#endif`,vh=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
	vec3 shadowWorldNormal = inverseTransformDirection( transformedNormal, viewMatrix );
	vec4 shadowWorldPosition;
#endif
#if defined( USE_SHADOWMAP )
	#if NUM_DIR_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * directionalLightShadows[ i ].shadowNormalBias, 0 );
			vDirectionalShadowCoord[ i ] = directionalShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * pointLightShadows[ i ].shadowNormalBias, 0 );
			vPointShadowCoord[ i ] = pointShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
#endif
#if NUM_SPOT_LIGHT_COORDS > 0
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_COORDS; i ++ ) {
		shadowWorldPosition = worldPosition;
		#if ( defined( USE_SHADOWMAP ) && UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
			shadowWorldPosition.xyz += shadowWorldNormal * spotLightShadows[ i ].shadowNormalBias;
		#endif
		vSpotLightCoord[ i ] = spotLightMatrix[ i ] * shadowWorldPosition;
	}
	#pragma unroll_loop_end
#endif`,xh=`float getShadowMask() {
	float shadow = 1.0;
	#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
		directionalLight = directionalLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( directionalShadowMap[ i ], directionalLight.shadowMapSize, directionalLight.shadowIntensity, directionalLight.shadowBias, directionalLight.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_SHADOWS; i ++ ) {
		spotLight = spotLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( spotShadowMap[ i ], spotLight.shadowMapSize, spotLight.shadowIntensity, spotLight.shadowBias, spotLight.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
	PointLightShadow pointLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
		pointLight = pointLightShadows[ i ];
		shadow *= receiveShadow ? getPointShadow( pointShadowMap[ i ], pointLight.shadowMapSize, pointLight.shadowIntensity, pointLight.shadowBias, pointLight.shadowRadius, vPointShadowCoord[ i ], pointLight.shadowCameraNear, pointLight.shadowCameraFar ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#endif
	return shadow;
}`,Mh=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,Sh=`#ifdef USE_SKINNING
	uniform mat4 bindMatrix;
	uniform mat4 bindMatrixInverse;
	uniform highp sampler2D boneTexture;
	mat4 getBoneMatrix( const in float i ) {
		int size = textureSize( boneTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( boneTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( boneTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( boneTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( boneTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
#endif`,Eh=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,yh=`#ifdef USE_SKINNING
	mat4 skinMatrix = mat4( 0.0 );
	skinMatrix += skinWeight.x * boneMatX;
	skinMatrix += skinWeight.y * boneMatY;
	skinMatrix += skinWeight.z * boneMatZ;
	skinMatrix += skinWeight.w * boneMatW;
	skinMatrix = bindMatrixInverse * skinMatrix * bindMatrix;
	objectNormal = vec4( skinMatrix * vec4( objectNormal, 0.0 ) ).xyz;
	#ifdef USE_TANGENT
		objectTangent = vec4( skinMatrix * vec4( objectTangent, 0.0 ) ).xyz;
	#endif
#endif`,Th=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,Ah=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,bh=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,wh=`#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
uniform float toneMappingExposure;
vec3 LinearToneMapping( vec3 color ) {
	return saturate( toneMappingExposure * color );
}
vec3 ReinhardToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	return saturate( color / ( vec3( 1.0 ) + color ) );
}
vec3 CineonToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	color = max( vec3( 0.0 ), color - 0.004 );
	return pow( ( color * ( 6.2 * color + 0.5 ) ) / ( color * ( 6.2 * color + 1.7 ) + 0.06 ), vec3( 2.2 ) );
}
vec3 RRTAndODTFit( vec3 v ) {
	vec3 a = v * ( v + 0.0245786 ) - 0.000090537;
	vec3 b = v * ( 0.983729 * v + 0.4329510 ) + 0.238081;
	return a / b;
}
vec3 ACESFilmicToneMapping( vec3 color ) {
	const mat3 ACESInputMat = mat3(
		vec3( 0.59719, 0.07600, 0.02840 ),		vec3( 0.35458, 0.90834, 0.13383 ),
		vec3( 0.04823, 0.01566, 0.83777 )
	);
	const mat3 ACESOutputMat = mat3(
		vec3(  1.60475, -0.10208, -0.00327 ),		vec3( -0.53108,  1.10813, -0.07276 ),
		vec3( -0.07367, -0.00605,  1.07602 )
	);
	color *= toneMappingExposure / 0.6;
	color = ACESInputMat * color;
	color = RRTAndODTFit( color );
	color = ACESOutputMat * color;
	return saturate( color );
}
const mat3 LINEAR_REC2020_TO_LINEAR_SRGB = mat3(
	vec3( 1.6605, - 0.1246, - 0.0182 ),
	vec3( - 0.5876, 1.1329, - 0.1006 ),
	vec3( - 0.0728, - 0.0083, 1.1187 )
);
const mat3 LINEAR_SRGB_TO_LINEAR_REC2020 = mat3(
	vec3( 0.6274, 0.0691, 0.0164 ),
	vec3( 0.3293, 0.9195, 0.0880 ),
	vec3( 0.0433, 0.0113, 0.8956 )
);
vec3 agxDefaultContrastApprox( vec3 x ) {
	vec3 x2 = x * x;
	vec3 x4 = x2 * x2;
	return + 15.5 * x4 * x2
		- 40.14 * x4 * x
		+ 31.96 * x4
		- 6.868 * x2 * x
		+ 0.4298 * x2
		+ 0.1191 * x
		- 0.00232;
}
vec3 AgXToneMapping( vec3 color ) {
	const mat3 AgXInsetMatrix = mat3(
		vec3( 0.856627153315983, 0.137318972929847, 0.11189821299995 ),
		vec3( 0.0951212405381588, 0.761241990602591, 0.0767994186031903 ),
		vec3( 0.0482516061458583, 0.101439036467562, 0.811302368396859 )
	);
	const mat3 AgXOutsetMatrix = mat3(
		vec3( 1.1271005818144368, - 0.1413297634984383, - 0.14132976349843826 ),
		vec3( - 0.11060664309660323, 1.157823702216272, - 0.11060664309660294 ),
		vec3( - 0.016493938717834573, - 0.016493938717834257, 1.2519364065950405 )
	);
	const float AgxMinEv = - 12.47393;	const float AgxMaxEv = 4.026069;
	color *= toneMappingExposure;
	color = LINEAR_SRGB_TO_LINEAR_REC2020 * color;
	color = AgXInsetMatrix * color;
	color = max( color, 1e-10 );	color = log2( color );
	color = ( color - AgxMinEv ) / ( AgxMaxEv - AgxMinEv );
	color = clamp( color, 0.0, 1.0 );
	color = agxDefaultContrastApprox( color );
	color = AgXOutsetMatrix * color;
	color = pow( max( vec3( 0.0 ), color ), vec3( 2.2 ) );
	color = LINEAR_REC2020_TO_LINEAR_SRGB * color;
	color = clamp( color, 0.0, 1.0 );
	return color;
}
vec3 NeutralToneMapping( vec3 color ) {
	const float StartCompression = 0.8 - 0.04;
	const float Desaturation = 0.15;
	color *= toneMappingExposure;
	float x = min( color.r, min( color.g, color.b ) );
	float offset = x < 0.08 ? x - 6.25 * x * x : 0.04;
	color -= offset;
	float peak = max( color.r, max( color.g, color.b ) );
	if ( peak < StartCompression ) return color;
	float d = 1. - StartCompression;
	float newPeak = 1. - d * d / ( peak + d - StartCompression );
	color *= newPeak / peak;
	float g = 1. - 1. / ( Desaturation * ( peak - newPeak ) + 1. );
	return mix( color, vec3( newPeak ), g );
}
vec3 CustomToneMapping( vec3 color ) { return color; }`,Rh=`#ifdef USE_TRANSMISSION
	material.transmission = transmission;
	material.transmissionAlpha = 1.0;
	material.thickness = thickness;
	material.attenuationDistance = attenuationDistance;
	material.attenuationColor = attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		material.transmission *= texture2D( transmissionMap, vTransmissionMapUv ).r;
	#endif
	#ifdef USE_THICKNESSMAP
		material.thickness *= texture2D( thicknessMap, vThicknessMapUv ).g;
	#endif
	vec3 pos = vWorldPosition;
	vec3 v = normalize( cameraPosition - pos );
	vec3 n = inverseTransformDirection( normal, viewMatrix );
	vec4 transmitted = getIBLVolumeRefraction(
		n, v, material.roughness, material.diffuseColor, material.specularColor, material.specularF90,
		pos, modelMatrix, viewMatrix, projectionMatrix, material.dispersion, material.ior, material.thickness,
		material.attenuationColor, material.attenuationDistance );
	material.transmissionAlpha = mix( material.transmissionAlpha, transmitted.a, material.transmission );
	totalDiffuse = mix( totalDiffuse, transmitted.rgb, material.transmission );
#endif`,Ch=`#ifdef USE_TRANSMISSION
	uniform float transmission;
	uniform float thickness;
	uniform float attenuationDistance;
	uniform vec3 attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		uniform sampler2D transmissionMap;
	#endif
	#ifdef USE_THICKNESSMAP
		uniform sampler2D thicknessMap;
	#endif
	uniform vec2 transmissionSamplerSize;
	uniform sampler2D transmissionSamplerMap;
	uniform mat4 modelMatrix;
	uniform mat4 projectionMatrix;
	varying vec3 vWorldPosition;
	float w0( float a ) {
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - a + 3.0 ) - 3.0 ) + 1.0 );
	}
	float w1( float a ) {
		return ( 1.0 / 6.0 ) * ( a *  a * ( 3.0 * a - 6.0 ) + 4.0 );
	}
	float w2( float a ){
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - 3.0 * a + 3.0 ) + 3.0 ) + 1.0 );
	}
	float w3( float a ) {
		return ( 1.0 / 6.0 ) * ( a * a * a );
	}
	float g0( float a ) {
		return w0( a ) + w1( a );
	}
	float g1( float a ) {
		return w2( a ) + w3( a );
	}
	float h0( float a ) {
		return - 1.0 + w1( a ) / ( w0( a ) + w1( a ) );
	}
	float h1( float a ) {
		return 1.0 + w3( a ) / ( w2( a ) + w3( a ) );
	}
	vec4 bicubic( sampler2D tex, vec2 uv, vec4 texelSize, float lod ) {
		uv = uv * texelSize.zw + 0.5;
		vec2 iuv = floor( uv );
		vec2 fuv = fract( uv );
		float g0x = g0( fuv.x );
		float g1x = g1( fuv.x );
		float h0x = h0( fuv.x );
		float h1x = h1( fuv.x );
		float h0y = h0( fuv.y );
		float h1y = h1( fuv.y );
		vec2 p0 = ( vec2( iuv.x + h0x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p1 = ( vec2( iuv.x + h1x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p2 = ( vec2( iuv.x + h0x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		vec2 p3 = ( vec2( iuv.x + h1x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		return g0( fuv.y ) * ( g0x * textureLod( tex, p0, lod ) + g1x * textureLod( tex, p1, lod ) ) +
			g1( fuv.y ) * ( g0x * textureLod( tex, p2, lod ) + g1x * textureLod( tex, p3, lod ) );
	}
	vec4 textureBicubic( sampler2D sampler, vec2 uv, float lod ) {
		vec2 fLodSize = vec2( textureSize( sampler, int( lod ) ) );
		vec2 cLodSize = vec2( textureSize( sampler, int( lod + 1.0 ) ) );
		vec2 fLodSizeInv = 1.0 / fLodSize;
		vec2 cLodSizeInv = 1.0 / cLodSize;
		vec4 fSample = bicubic( sampler, uv, vec4( fLodSizeInv, fLodSize ), floor( lod ) );
		vec4 cSample = bicubic( sampler, uv, vec4( cLodSizeInv, cLodSize ), ceil( lod ) );
		return mix( fSample, cSample, fract( lod ) );
	}
	vec3 getVolumeTransmissionRay( const in vec3 n, const in vec3 v, const in float thickness, const in float ior, const in mat4 modelMatrix ) {
		vec3 refractionVector = refract( - v, normalize( n ), 1.0 / ior );
		vec3 modelScale;
		modelScale.x = length( vec3( modelMatrix[ 0 ].xyz ) );
		modelScale.y = length( vec3( modelMatrix[ 1 ].xyz ) );
		modelScale.z = length( vec3( modelMatrix[ 2 ].xyz ) );
		return normalize( refractionVector ) * thickness * modelScale;
	}
	float applyIorToRoughness( const in float roughness, const in float ior ) {
		return roughness * clamp( ior * 2.0 - 2.0, 0.0, 1.0 );
	}
	vec4 getTransmissionSample( const in vec2 fragCoord, const in float roughness, const in float ior ) {
		float lod = log2( transmissionSamplerSize.x ) * applyIorToRoughness( roughness, ior );
		return textureBicubic( transmissionSamplerMap, fragCoord.xy, lod );
	}
	vec3 volumeAttenuation( const in float transmissionDistance, const in vec3 attenuationColor, const in float attenuationDistance ) {
		if ( isinf( attenuationDistance ) ) {
			return vec3( 1.0 );
		} else {
			vec3 attenuationCoefficient = -log( attenuationColor ) / attenuationDistance;
			vec3 transmittance = exp( - attenuationCoefficient * transmissionDistance );			return transmittance;
		}
	}
	vec4 getIBLVolumeRefraction( const in vec3 n, const in vec3 v, const in float roughness, const in vec3 diffuseColor,
		const in vec3 specularColor, const in float specularF90, const in vec3 position, const in mat4 modelMatrix,
		const in mat4 viewMatrix, const in mat4 projMatrix, const in float dispersion, const in float ior, const in float thickness,
		const in vec3 attenuationColor, const in float attenuationDistance ) {
		vec4 transmittedLight;
		vec3 transmittance;
		#ifdef USE_DISPERSION
			float halfSpread = ( ior - 1.0 ) * 0.025 * dispersion;
			vec3 iors = vec3( ior - halfSpread, ior, ior + halfSpread );
			for ( int i = 0; i < 3; i ++ ) {
				vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, iors[ i ], modelMatrix );
				vec3 refractedRayExit = position + transmissionRay;
		
				vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
				vec2 refractionCoords = ndcPos.xy / ndcPos.w;
				refractionCoords += 1.0;
				refractionCoords /= 2.0;
		
				vec4 transmissionSample = getTransmissionSample( refractionCoords, roughness, iors[ i ] );
				transmittedLight[ i ] = transmissionSample[ i ];
				transmittedLight.a += transmissionSample.a;
				transmittance[ i ] = diffuseColor[ i ] * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance )[ i ];
			}
			transmittedLight.a /= 3.0;
		
		#else
		
			vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, ior, modelMatrix );
			vec3 refractedRayExit = position + transmissionRay;
			vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
			vec2 refractionCoords = ndcPos.xy / ndcPos.w;
			refractionCoords += 1.0;
			refractionCoords /= 2.0;
			transmittedLight = getTransmissionSample( refractionCoords, roughness, ior );
			transmittance = diffuseColor * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance );
		
		#endif
		vec3 attenuatedColor = transmittance * transmittedLight.rgb;
		vec3 F = EnvironmentBRDF( n, v, specularColor, specularF90, roughness );
		float transmittanceFactor = ( transmittance.r + transmittance.g + transmittance.b ) / 3.0;
		return vec4( ( 1.0 - F ) * attenuatedColor, 1.0 - ( 1.0 - transmittedLight.a ) * transmittanceFactor );
	}
#endif`,Ph=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_SPECULARMAP
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,Lh=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	uniform mat3 mapTransform;
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	uniform mat3 alphaMapTransform;
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	uniform mat3 lightMapTransform;
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	uniform mat3 aoMapTransform;
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	uniform mat3 bumpMapTransform;
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	uniform mat3 normalMapTransform;
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_DISPLACEMENTMAP
	uniform mat3 displacementMapTransform;
	varying vec2 vDisplacementMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	uniform mat3 emissiveMapTransform;
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	uniform mat3 metalnessMapTransform;
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	uniform mat3 roughnessMapTransform;
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	uniform mat3 anisotropyMapTransform;
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	uniform mat3 clearcoatMapTransform;
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform mat3 clearcoatNormalMapTransform;
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform mat3 clearcoatRoughnessMapTransform;
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	uniform mat3 sheenColorMapTransform;
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	uniform mat3 sheenRoughnessMapTransform;
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	uniform mat3 iridescenceMapTransform;
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform mat3 iridescenceThicknessMapTransform;
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SPECULARMAP
	uniform mat3 specularMapTransform;
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	uniform mat3 specularColorMapTransform;
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	uniform mat3 specularIntensityMapTransform;
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,Dh=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	vUv = vec3( uv, 1 ).xy;
#endif
#ifdef USE_MAP
	vMapUv = ( mapTransform * vec3( MAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ALPHAMAP
	vAlphaMapUv = ( alphaMapTransform * vec3( ALPHAMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_LIGHTMAP
	vLightMapUv = ( lightMapTransform * vec3( LIGHTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_AOMAP
	vAoMapUv = ( aoMapTransform * vec3( AOMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_BUMPMAP
	vBumpMapUv = ( bumpMapTransform * vec3( BUMPMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_NORMALMAP
	vNormalMapUv = ( normalMapTransform * vec3( NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_DISPLACEMENTMAP
	vDisplacementMapUv = ( displacementMapTransform * vec3( DISPLACEMENTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_EMISSIVEMAP
	vEmissiveMapUv = ( emissiveMapTransform * vec3( EMISSIVEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_METALNESSMAP
	vMetalnessMapUv = ( metalnessMapTransform * vec3( METALNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ROUGHNESSMAP
	vRoughnessMapUv = ( roughnessMapTransform * vec3( ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ANISOTROPYMAP
	vAnisotropyMapUv = ( anisotropyMapTransform * vec3( ANISOTROPYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOATMAP
	vClearcoatMapUv = ( clearcoatMapTransform * vec3( CLEARCOATMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	vClearcoatNormalMapUv = ( clearcoatNormalMapTransform * vec3( CLEARCOAT_NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	vClearcoatRoughnessMapUv = ( clearcoatRoughnessMapTransform * vec3( CLEARCOAT_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCEMAP
	vIridescenceMapUv = ( iridescenceMapTransform * vec3( IRIDESCENCEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	vIridescenceThicknessMapUv = ( iridescenceThicknessMapTransform * vec3( IRIDESCENCE_THICKNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_COLORMAP
	vSheenColorMapUv = ( sheenColorMapTransform * vec3( SHEEN_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	vSheenRoughnessMapUv = ( sheenRoughnessMapTransform * vec3( SHEEN_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULARMAP
	vSpecularMapUv = ( specularMapTransform * vec3( SPECULARMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_COLORMAP
	vSpecularColorMapUv = ( specularColorMapTransform * vec3( SPECULAR_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	vSpecularIntensityMapUv = ( specularIntensityMapTransform * vec3( SPECULAR_INTENSITYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_TRANSMISSIONMAP
	vTransmissionMapUv = ( transmissionMapTransform * vec3( TRANSMISSIONMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_THICKNESSMAP
	vThicknessMapUv = ( thicknessMapTransform * vec3( THICKNESSMAP_UV, 1 ) ).xy;
#endif`,Uh=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const Ih=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,Nh=`uniform sampler2D t2D;
uniform float backgroundIntensity;
varying vec2 vUv;
void main() {
	vec4 texColor = texture2D( t2D, vUv );
	#ifdef DECODE_VIDEO_TEXTURE
		texColor = vec4( mix( pow( texColor.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), texColor.rgb * 0.0773993808, vec3( lessThanEqual( texColor.rgb, vec3( 0.04045 ) ) ) ), texColor.w );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,Fh=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,Oh=`#ifdef ENVMAP_TYPE_CUBE
	uniform samplerCube envMap;
#elif defined( ENVMAP_TYPE_CUBE_UV )
	uniform sampler2D envMap;
#endif
uniform float flipEnvMap;
uniform float backgroundBlurriness;
uniform float backgroundIntensity;
uniform mat3 backgroundRotation;
varying vec3 vWorldDirection;
#include <cube_uv_reflection_fragment>
void main() {
	#ifdef ENVMAP_TYPE_CUBE
		vec4 texColor = textureCube( envMap, backgroundRotation * vec3( flipEnvMap * vWorldDirection.x, vWorldDirection.yz ) );
	#elif defined( ENVMAP_TYPE_CUBE_UV )
		vec4 texColor = textureCubeUV( envMap, backgroundRotation * vWorldDirection, backgroundBlurriness );
	#else
		vec4 texColor = vec4( 0.0, 0.0, 0.0, 1.0 );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,Bh=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,zh=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,Hh=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
varying vec2 vHighPrecisionZW;
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vHighPrecisionZW = gl_Position.zw;
}`,Gh=`#if DEPTH_PACKING == 3200
	uniform float opacity;
#endif
#include <common>
#include <packing>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
varying vec2 vHighPrecisionZW;
void main() {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#if DEPTH_PACKING == 3200
		diffuseColor.a = opacity;
	#endif
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <logdepthbuf_fragment>
	float fragCoordZ = 0.5 * vHighPrecisionZW[0] / vHighPrecisionZW[1] + 0.5;
	#if DEPTH_PACKING == 3200
		gl_FragColor = vec4( vec3( 1.0 - fragCoordZ ), opacity );
	#elif DEPTH_PACKING == 3201
		gl_FragColor = packDepthToRGBA( fragCoordZ );
	#elif DEPTH_PACKING == 3202
		gl_FragColor = vec4( packDepthToRGB( fragCoordZ ), 1.0 );
	#elif DEPTH_PACKING == 3203
		gl_FragColor = vec4( packDepthToRG( fragCoordZ ), 0.0, 1.0 );
	#endif
}`,Vh=`#define DISTANCE
varying vec3 vWorldPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <worldpos_vertex>
	#include <clipping_planes_vertex>
	vWorldPosition = worldPosition.xyz;
}`,kh=`#define DISTANCE
uniform vec3 referencePosition;
uniform float nearDistance;
uniform float farDistance;
varying vec3 vWorldPosition;
#include <common>
#include <packing>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <clipping_planes_pars_fragment>
void main () {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	float dist = length( vWorldPosition - referencePosition );
	dist = ( dist - nearDistance ) / ( farDistance - nearDistance );
	dist = saturate( dist );
	gl_FragColor = packDepthToRGBA( dist );
}`,Wh=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,Xh=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,qh=`uniform float scale;
attribute float lineDistance;
varying float vLineDistance;
#include <common>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	vLineDistance = scale * lineDistance;
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,Yh=`uniform vec3 diffuse;
uniform float opacity;
uniform float dashSize;
uniform float totalSize;
varying float vLineDistance;
#include <common>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	if ( mod( vLineDistance, totalSize ) > dashSize ) {
		discard;
	}
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,Kh=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#if defined ( USE_ENVMAP ) || defined ( USE_SKINNING )
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinbase_vertex>
		#include <skinnormal_vertex>
		#include <defaultnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <fog_vertex>
}`,$h=`uniform vec3 diffuse;
uniform float opacity;
#ifndef FLAT_SHADED
	varying vec3 vNormal;
#endif
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <fog_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		reflectedLight.indirectDiffuse += lightMapTexel.rgb * lightMapIntensity * RECIPROCAL_PI;
	#else
		reflectedLight.indirectDiffuse += vec3( 1.0 );
	#endif
	#include <aomap_fragment>
	reflectedLight.indirectDiffuse *= diffuseColor.rgb;
	vec3 outgoingLight = reflectedLight.indirectDiffuse;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,Zh=`#define LAMBERT
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,jh=`#define LAMBERT
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_lambert_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_lambert_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,Jh=`#define MATCAP
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
	vViewPosition = - mvPosition.xyz;
}`,Qh=`#define MATCAP
uniform vec3 diffuse;
uniform float opacity;
uniform sampler2D matcap;
varying vec3 vViewPosition;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	vec3 viewDir = normalize( vViewPosition );
	vec3 x = normalize( vec3( viewDir.z, 0.0, - viewDir.x ) );
	vec3 y = cross( viewDir, x );
	vec2 uv = vec2( dot( x, normal ), dot( y, normal ) ) * 0.495 + 0.5;
	#ifdef USE_MATCAP
		vec4 matcapColor = texture2D( matcap, uv );
	#else
		vec4 matcapColor = vec4( vec3( mix( 0.2, 0.8, uv.y ) ), 1.0 );
	#endif
	vec3 outgoingLight = diffuseColor.rgb * matcapColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,ed=`#define NORMAL
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	vViewPosition = - mvPosition.xyz;
#endif
}`,td=`#define NORMAL
uniform float opacity;
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <packing>
#include <uv_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( 0.0, 0.0, 0.0, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	gl_FragColor = vec4( packNormalToRGB( normal ), diffuseColor.a );
	#ifdef OPAQUE
		gl_FragColor.a = 1.0;
	#endif
}`,nd=`#define PHONG
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,id=`#define PHONG
uniform vec3 diffuse;
uniform vec3 emissive;
uniform vec3 specular;
uniform float shininess;
uniform float opacity;
#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_phong_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_phong_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,rd=`#define STANDARD
varying vec3 vViewPosition;
#ifdef USE_TRANSMISSION
	varying vec3 vWorldPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
#ifdef USE_TRANSMISSION
	vWorldPosition = worldPosition.xyz;
#endif
}`,sd=`#define STANDARD
#ifdef PHYSICAL
	#define IOR
	#define USE_SPECULAR
#endif
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;
#ifdef IOR
	uniform float ior;
#endif
#ifdef USE_SPECULAR
	uniform float specularIntensity;
	uniform vec3 specularColor;
	#ifdef USE_SPECULAR_COLORMAP
		uniform sampler2D specularColorMap;
	#endif
	#ifdef USE_SPECULAR_INTENSITYMAP
		uniform sampler2D specularIntensityMap;
	#endif
#endif
#ifdef USE_CLEARCOAT
	uniform float clearcoat;
	uniform float clearcoatRoughness;
#endif
#ifdef USE_DISPERSION
	uniform float dispersion;
#endif
#ifdef USE_IRIDESCENCE
	uniform float iridescence;
	uniform float iridescenceIOR;
	uniform float iridescenceThicknessMinimum;
	uniform float iridescenceThicknessMaximum;
#endif
#ifdef USE_SHEEN
	uniform vec3 sheenColor;
	uniform float sheenRoughness;
	#ifdef USE_SHEEN_COLORMAP
		uniform sampler2D sheenColorMap;
	#endif
	#ifdef USE_SHEEN_ROUGHNESSMAP
		uniform sampler2D sheenRoughnessMap;
	#endif
#endif
#ifdef USE_ANISOTROPY
	uniform vec2 anisotropyVector;
	#ifdef USE_ANISOTROPYMAP
		uniform sampler2D anisotropyMap;
	#endif
#endif
varying vec3 vViewPosition;
#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <iridescence_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_physical_pars_fragment>
#include <transmission_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <iridescence_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <roughnessmap_fragment>
	#include <metalnessmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <clearcoat_normal_fragment_begin>
	#include <clearcoat_normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_physical_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 totalDiffuse = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse;
	vec3 totalSpecular = reflectedLight.directSpecular + reflectedLight.indirectSpecular;
	#include <transmission_fragment>
	vec3 outgoingLight = totalDiffuse + totalSpecular + totalEmissiveRadiance;
	#ifdef USE_SHEEN
		float sheenEnergyComp = 1.0 - 0.157 * max3( material.sheenColor );
		outgoingLight = outgoingLight * sheenEnergyComp + sheenSpecularDirect + sheenSpecularIndirect;
	#endif
	#ifdef USE_CLEARCOAT
		float dotNVcc = saturate( dot( geometryClearcoatNormal, geometryViewDir ) );
		vec3 Fcc = F_Schlick( material.clearcoatF0, material.clearcoatF90, dotNVcc );
		outgoingLight = outgoingLight * ( 1.0 - material.clearcoat * Fcc ) + ( clearcoatSpecularDirect + clearcoatSpecularIndirect ) * material.clearcoat;
	#endif
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,ad=`#define TOON
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,od=`#define TOON
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <packing>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <gradientmap_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_toon_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_toon_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,ld=`uniform float size;
uniform float scale;
#include <common>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
#ifdef USE_POINTS_UV
	varying vec2 vUv;
	uniform mat3 uvTransform;
#endif
void main() {
	#ifdef USE_POINTS_UV
		vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	#endif
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	gl_PointSize = size;
	#ifdef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) gl_PointSize *= ( scale / - mvPosition.z );
	#endif
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <fog_vertex>
}`,cd=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <color_pars_fragment>
#include <map_particle_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_particle_fragment>
	#include <color_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,ud=`#include <common>
#include <batching_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <shadowmap_pars_vertex>
void main() {
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,hd=`uniform vec3 color;
uniform float opacity;
#include <common>
#include <packing>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <logdepthbuf_pars_fragment>
#include <shadowmap_pars_fragment>
#include <shadowmask_pars_fragment>
void main() {
	#include <logdepthbuf_fragment>
	gl_FragColor = vec4( color, opacity * ( 1.0 - getShadowMask() ) );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
}`,dd=`uniform float rotation;
uniform vec2 center;
#include <common>
#include <uv_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	vec4 mvPosition = modelViewMatrix[ 3 ];
	vec2 scale = vec2( length( modelMatrix[ 0 ].xyz ), length( modelMatrix[ 1 ].xyz ) );
	#ifndef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) scale *= - mvPosition.z;
	#endif
	vec2 alignedPosition = ( position.xy - ( center - vec2( 0.5 ) ) ) * scale;
	vec2 rotatedPosition;
	rotatedPosition.x = cos( rotation ) * alignedPosition.x - sin( rotation ) * alignedPosition.y;
	rotatedPosition.y = sin( rotation ) * alignedPosition.x + cos( rotation ) * alignedPosition.y;
	mvPosition.xy += rotatedPosition;
	gl_Position = projectionMatrix * mvPosition;
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,fd=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
}`,Le={alphahash_fragment:Nc,alphahash_pars_fragment:Fc,alphamap_fragment:Oc,alphamap_pars_fragment:Bc,alphatest_fragment:zc,alphatest_pars_fragment:Hc,aomap_fragment:Gc,aomap_pars_fragment:Vc,batching_pars_vertex:kc,batching_vertex:Wc,begin_vertex:Xc,beginnormal_vertex:qc,bsdfs:Yc,iridescence_fragment:Kc,bumpmap_pars_fragment:$c,clipping_planes_fragment:Zc,clipping_planes_pars_fragment:jc,clipping_planes_pars_vertex:Jc,clipping_planes_vertex:Qc,color_fragment:eu,color_pars_fragment:tu,color_pars_vertex:nu,color_vertex:iu,common:ru,cube_uv_reflection_fragment:su,defaultnormal_vertex:au,displacementmap_pars_vertex:ou,displacementmap_vertex:lu,emissivemap_fragment:cu,emissivemap_pars_fragment:uu,colorspace_fragment:hu,colorspace_pars_fragment:du,envmap_fragment:fu,envmap_common_pars_fragment:pu,envmap_pars_fragment:mu,envmap_pars_vertex:_u,envmap_physical_pars_fragment:wu,envmap_vertex:gu,fog_vertex:vu,fog_pars_vertex:xu,fog_fragment:Mu,fog_pars_fragment:Su,gradientmap_pars_fragment:Eu,lightmap_pars_fragment:yu,lights_lambert_fragment:Tu,lights_lambert_pars_fragment:Au,lights_pars_begin:bu,lights_toon_fragment:Ru,lights_toon_pars_fragment:Cu,lights_phong_fragment:Pu,lights_phong_pars_fragment:Lu,lights_physical_fragment:Du,lights_physical_pars_fragment:Uu,lights_fragment_begin:Iu,lights_fragment_maps:Nu,lights_fragment_end:Fu,logdepthbuf_fragment:Ou,logdepthbuf_pars_fragment:Bu,logdepthbuf_pars_vertex:zu,logdepthbuf_vertex:Hu,map_fragment:Gu,map_pars_fragment:Vu,map_particle_fragment:ku,map_particle_pars_fragment:Wu,metalnessmap_fragment:Xu,metalnessmap_pars_fragment:qu,morphinstance_vertex:Yu,morphcolor_vertex:Ku,morphnormal_vertex:$u,morphtarget_pars_vertex:Zu,morphtarget_vertex:ju,normal_fragment_begin:Ju,normal_fragment_maps:Qu,normal_pars_fragment:eh,normal_pars_vertex:th,normal_vertex:nh,normalmap_pars_fragment:ih,clearcoat_normal_fragment_begin:rh,clearcoat_normal_fragment_maps:sh,clearcoat_pars_fragment:ah,iridescence_pars_fragment:oh,opaque_fragment:lh,packing:ch,premultiplied_alpha_fragment:uh,project_vertex:hh,dithering_fragment:dh,dithering_pars_fragment:fh,roughnessmap_fragment:ph,roughnessmap_pars_fragment:mh,shadowmap_pars_fragment:_h,shadowmap_pars_vertex:gh,shadowmap_vertex:vh,shadowmask_pars_fragment:xh,skinbase_vertex:Mh,skinning_pars_vertex:Sh,skinning_vertex:Eh,skinnormal_vertex:yh,specularmap_fragment:Th,specularmap_pars_fragment:Ah,tonemapping_fragment:bh,tonemapping_pars_fragment:wh,transmission_fragment:Rh,transmission_pars_fragment:Ch,uv_pars_fragment:Ph,uv_pars_vertex:Lh,uv_vertex:Dh,worldpos_vertex:Uh,background_vert:Ih,background_frag:Nh,backgroundCube_vert:Fh,backgroundCube_frag:Oh,cube_vert:Bh,cube_frag:zh,depth_vert:Hh,depth_frag:Gh,distanceRGBA_vert:Vh,distanceRGBA_frag:kh,equirect_vert:Wh,equirect_frag:Xh,linedashed_vert:qh,linedashed_frag:Yh,meshbasic_vert:Kh,meshbasic_frag:$h,meshlambert_vert:Zh,meshlambert_frag:jh,meshmatcap_vert:Jh,meshmatcap_frag:Qh,meshnormal_vert:ed,meshnormal_frag:td,meshphong_vert:nd,meshphong_frag:id,meshphysical_vert:rd,meshphysical_frag:sd,meshtoon_vert:ad,meshtoon_frag:od,points_vert:ld,points_frag:cd,shadow_vert:ud,shadow_frag:hd,sprite_vert:dd,sprite_frag:fd},re={common:{diffuse:{value:new Oe(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new De},alphaMap:{value:null},alphaMapTransform:{value:new De},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new De}},envmap:{envMap:{value:null},envMapRotation:{value:new De},flipEnvMap:{value:-1},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new De}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new De}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new De},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new De},normalScale:{value:new ze(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new De},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new De}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new De}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new De}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new Oe(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMap:{value:[]},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotShadowMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMap:{value:[]},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null}},points:{diffuse:{value:new Oe(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new De},alphaTest:{value:0},uvTransform:{value:new De}},sprite:{diffuse:{value:new Oe(16777215)},opacity:{value:1},center:{value:new ze(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new De},alphaMap:{value:null},alphaMapTransform:{value:new De},alphaTest:{value:0}}},kt={basic:{uniforms:pt([re.common,re.specularmap,re.envmap,re.aomap,re.lightmap,re.fog]),vertexShader:Le.meshbasic_vert,fragmentShader:Le.meshbasic_frag},lambert:{uniforms:pt([re.common,re.specularmap,re.envmap,re.aomap,re.lightmap,re.emissivemap,re.bumpmap,re.normalmap,re.displacementmap,re.fog,re.lights,{emissive:{value:new Oe(0)}}]),vertexShader:Le.meshlambert_vert,fragmentShader:Le.meshlambert_frag},phong:{uniforms:pt([re.common,re.specularmap,re.envmap,re.aomap,re.lightmap,re.emissivemap,re.bumpmap,re.normalmap,re.displacementmap,re.fog,re.lights,{emissive:{value:new Oe(0)},specular:{value:new Oe(1118481)},shininess:{value:30}}]),vertexShader:Le.meshphong_vert,fragmentShader:Le.meshphong_frag},standard:{uniforms:pt([re.common,re.envmap,re.aomap,re.lightmap,re.emissivemap,re.bumpmap,re.normalmap,re.displacementmap,re.roughnessmap,re.metalnessmap,re.fog,re.lights,{emissive:{value:new Oe(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:Le.meshphysical_vert,fragmentShader:Le.meshphysical_frag},toon:{uniforms:pt([re.common,re.aomap,re.lightmap,re.emissivemap,re.bumpmap,re.normalmap,re.displacementmap,re.gradientmap,re.fog,re.lights,{emissive:{value:new Oe(0)}}]),vertexShader:Le.meshtoon_vert,fragmentShader:Le.meshtoon_frag},matcap:{uniforms:pt([re.common,re.bumpmap,re.normalmap,re.displacementmap,re.fog,{matcap:{value:null}}]),vertexShader:Le.meshmatcap_vert,fragmentShader:Le.meshmatcap_frag},points:{uniforms:pt([re.points,re.fog]),vertexShader:Le.points_vert,fragmentShader:Le.points_frag},dashed:{uniforms:pt([re.common,re.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:Le.linedashed_vert,fragmentShader:Le.linedashed_frag},depth:{uniforms:pt([re.common,re.displacementmap]),vertexShader:Le.depth_vert,fragmentShader:Le.depth_frag},normal:{uniforms:pt([re.common,re.bumpmap,re.normalmap,re.displacementmap,{opacity:{value:1}}]),vertexShader:Le.meshnormal_vert,fragmentShader:Le.meshnormal_frag},sprite:{uniforms:pt([re.sprite,re.fog]),vertexShader:Le.sprite_vert,fragmentShader:Le.sprite_frag},background:{uniforms:{uvTransform:{value:new De},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:Le.background_vert,fragmentShader:Le.background_frag},backgroundCube:{uniforms:{envMap:{value:null},flipEnvMap:{value:-1},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new De}},vertexShader:Le.backgroundCube_vert,fragmentShader:Le.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:Le.cube_vert,fragmentShader:Le.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:Le.equirect_vert,fragmentShader:Le.equirect_frag},distanceRGBA:{uniforms:pt([re.common,re.displacementmap,{referencePosition:{value:new D},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:Le.distanceRGBA_vert,fragmentShader:Le.distanceRGBA_frag},shadow:{uniforms:pt([re.lights,re.fog,{color:{value:new Oe(0)},opacity:{value:1}}]),vertexShader:Le.shadow_vert,fragmentShader:Le.shadow_frag}};kt.physical={uniforms:pt([kt.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new De},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new De},clearcoatNormalScale:{value:new ze(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new De},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new De},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new De},sheen:{value:0},sheenColor:{value:new Oe(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new De},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new De},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new De},transmissionSamplerSize:{value:new ze},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new De},attenuationDistance:{value:0},attenuationColor:{value:new Oe(0)},specularColor:{value:new Oe(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new De},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new De},anisotropyVector:{value:new ze},anisotropyMap:{value:null},anisotropyMapTransform:{value:new De}}]),vertexShader:Le.meshphysical_vert,fragmentShader:Le.meshphysical_frag};const qi={r:0,b:0,g:0},En=new Xt,pd=new et;function md(i,e,t,n,r,s,a){const o=new Oe(0);let l=s===!0?0:1,c,d,p=null,f=0,m=null;function _(y){let S=y.isScene===!0?y.background:null;return S&&S.isTexture&&(S=(y.backgroundBlurriness>0?t:e).get(S)),S}function M(y){let S=!1;const T=_(y);T===null?u(o,l):T&&T.isColor&&(u(T,1),S=!0);const N=i.xr.getEnvironmentBlendMode();N==="additive"?n.buffers.color.setClear(0,0,0,1,a):N==="alpha-blend"&&n.buffers.color.setClear(0,0,0,0,a),(i.autoClear||S)&&(n.buffers.depth.setTest(!0),n.buffers.depth.setMask(!0),n.buffers.color.setMask(!0),i.clear(i.autoClearColor,i.autoClearDepth,i.autoClearStencil))}function h(y,S){const T=_(S);T&&(T.isCubeTexture||T.mapping===fr)?(d===void 0&&(d=new wt(new yi(1,1,1),new mn({name:"BackgroundCubeMaterial",uniforms:ni(kt.backgroundCube.uniforms),vertexShader:kt.backgroundCube.vertexShader,fragmentShader:kt.backgroundCube.fragmentShader,side:Mt,depthTest:!1,depthWrite:!1,fog:!1})),d.geometry.deleteAttribute("normal"),d.geometry.deleteAttribute("uv"),d.onBeforeRender=function(N,R,w){this.matrixWorld.copyPosition(w.matrixWorld)},Object.defineProperty(d.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),r.update(d)),En.copy(S.backgroundRotation),En.x*=-1,En.y*=-1,En.z*=-1,T.isCubeTexture&&T.isRenderTargetTexture===!1&&(En.y*=-1,En.z*=-1),d.material.uniforms.envMap.value=T,d.material.uniforms.flipEnvMap.value=T.isCubeTexture&&T.isRenderTargetTexture===!1?-1:1,d.material.uniforms.backgroundBlurriness.value=S.backgroundBlurriness,d.material.uniforms.backgroundIntensity.value=S.backgroundIntensity,d.material.uniforms.backgroundRotation.value.setFromMatrix4(pd.makeRotationFromEuler(En)),d.material.toneMapped=Ke.getTransfer(T.colorSpace)!==Qe,(p!==T||f!==T.version||m!==i.toneMapping)&&(d.material.needsUpdate=!0,p=T,f=T.version,m=i.toneMapping),d.layers.enableAll(),y.unshift(d,d.geometry,d.material,0,0,null)):T&&T.isTexture&&(c===void 0&&(c=new wt(new _r(2,2),new mn({name:"BackgroundMaterial",uniforms:ni(kt.background.uniforms),vertexShader:kt.background.vertexShader,fragmentShader:kt.background.fragmentShader,side:pn,depthTest:!1,depthWrite:!1,fog:!1})),c.geometry.deleteAttribute("normal"),Object.defineProperty(c.material,"map",{get:function(){return this.uniforms.t2D.value}}),r.update(c)),c.material.uniforms.t2D.value=T,c.material.uniforms.backgroundIntensity.value=S.backgroundIntensity,c.material.toneMapped=Ke.getTransfer(T.colorSpace)!==Qe,T.matrixAutoUpdate===!0&&T.updateMatrix(),c.material.uniforms.uvTransform.value.copy(T.matrix),(p!==T||f!==T.version||m!==i.toneMapping)&&(c.material.needsUpdate=!0,p=T,f=T.version,m=i.toneMapping),c.layers.enableAll(),y.unshift(c,c.geometry,c.material,0,0,null))}function u(y,S){y.getRGB(qi,Go(i)),n.buffers.color.setClear(qi.r,qi.g,qi.b,S,a)}return{getClearColor:function(){return o},setClearColor:function(y,S=1){o.set(y),l=S,u(o,l)},getClearAlpha:function(){return l},setClearAlpha:function(y){l=y,u(o,l)},render:M,addToRenderList:h}}function _d(i,e){const t=i.getParameter(i.MAX_VERTEX_ATTRIBS),n={},r=f(null);let s=r,a=!1;function o(g,E,k,G,X){let J=!1;const H=p(G,k,E);s!==H&&(s=H,c(s.object)),J=m(g,G,k,X),J&&_(g,G,k,X),X!==null&&e.update(X,i.ELEMENT_ARRAY_BUFFER),(J||a)&&(a=!1,T(g,E,k,G),X!==null&&i.bindBuffer(i.ELEMENT_ARRAY_BUFFER,e.get(X).buffer))}function l(){return i.createVertexArray()}function c(g){return i.bindVertexArray(g)}function d(g){return i.deleteVertexArray(g)}function p(g,E,k){const G=k.wireframe===!0;let X=n[g.id];X===void 0&&(X={},n[g.id]=X);let J=X[E.id];J===void 0&&(J={},X[E.id]=J);let H=J[G];return H===void 0&&(H=f(l()),J[G]=H),H}function f(g){const E=[],k=[],G=[];for(let X=0;X<t;X++)E[X]=0,k[X]=0,G[X]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:E,enabledAttributes:k,attributeDivisors:G,object:g,attributes:{},index:null}}function m(g,E,k,G){const X=s.attributes,J=E.attributes;let H=0;const Q=k.getAttributes();for(const W in Q)if(Q[W].location>=0){const ce=X[W];let _e=J[W];if(_e===void 0&&(W==="instanceMatrix"&&g.instanceMatrix&&(_e=g.instanceMatrix),W==="instanceColor"&&g.instanceColor&&(_e=g.instanceColor)),ce===void 0||ce.attribute!==_e||_e&&ce.data!==_e.data)return!0;H++}return s.attributesNum!==H||s.index!==G}function _(g,E,k,G){const X={},J=E.attributes;let H=0;const Q=k.getAttributes();for(const W in Q)if(Q[W].location>=0){let ce=J[W];ce===void 0&&(W==="instanceMatrix"&&g.instanceMatrix&&(ce=g.instanceMatrix),W==="instanceColor"&&g.instanceColor&&(ce=g.instanceColor));const _e={};_e.attribute=ce,ce&&ce.data&&(_e.data=ce.data),X[W]=_e,H++}s.attributes=X,s.attributesNum=H,s.index=G}function M(){const g=s.newAttributes;for(let E=0,k=g.length;E<k;E++)g[E]=0}function h(g){u(g,0)}function u(g,E){const k=s.newAttributes,G=s.enabledAttributes,X=s.attributeDivisors;k[g]=1,G[g]===0&&(i.enableVertexAttribArray(g),G[g]=1),X[g]!==E&&(i.vertexAttribDivisor(g,E),X[g]=E)}function y(){const g=s.newAttributes,E=s.enabledAttributes;for(let k=0,G=E.length;k<G;k++)E[k]!==g[k]&&(i.disableVertexAttribArray(k),E[k]=0)}function S(g,E,k,G,X,J,H){H===!0?i.vertexAttribIPointer(g,E,k,X,J):i.vertexAttribPointer(g,E,k,G,X,J)}function T(g,E,k,G){M();const X=G.attributes,J=k.getAttributes(),H=E.defaultAttributeValues;for(const Q in J){const W=J[Q];if(W.location>=0){let le=X[Q];if(le===void 0&&(Q==="instanceMatrix"&&g.instanceMatrix&&(le=g.instanceMatrix),Q==="instanceColor"&&g.instanceColor&&(le=g.instanceColor)),le!==void 0){const ce=le.normalized,_e=le.itemSize,He=e.get(le);if(He===void 0)continue;const Ve=He.buffer,b=He.type,B=He.bytesPerElement,ie=b===i.INT||b===i.UNSIGNED_INT||le.gpuType===qs;if(le.isInterleavedBufferAttribute){const Z=le.data,ge=Z.stride,ee=le.offset;if(Z.isInstancedInterleavedBuffer){for(let be=0;be<W.locationSize;be++)u(W.location+be,Z.meshPerAttribute);g.isInstancedMesh!==!0&&G._maxInstanceCount===void 0&&(G._maxInstanceCount=Z.meshPerAttribute*Z.count)}else for(let be=0;be<W.locationSize;be++)h(W.location+be);i.bindBuffer(i.ARRAY_BUFFER,Ve);for(let be=0;be<W.locationSize;be++)S(W.location+be,_e/W.locationSize,b,ce,ge*B,(ee+_e/W.locationSize*be)*B,ie)}else{if(le.isInstancedBufferAttribute){for(let Z=0;Z<W.locationSize;Z++)u(W.location+Z,le.meshPerAttribute);g.isInstancedMesh!==!0&&G._maxInstanceCount===void 0&&(G._maxInstanceCount=le.meshPerAttribute*le.count)}else for(let Z=0;Z<W.locationSize;Z++)h(W.location+Z);i.bindBuffer(i.ARRAY_BUFFER,Ve);for(let Z=0;Z<W.locationSize;Z++)S(W.location+Z,_e/W.locationSize,b,ce,_e*B,_e/W.locationSize*Z*B,ie)}}else if(H!==void 0){const ce=H[Q];if(ce!==void 0)switch(ce.length){case 2:i.vertexAttrib2fv(W.location,ce);break;case 3:i.vertexAttrib3fv(W.location,ce);break;case 4:i.vertexAttrib4fv(W.location,ce);break;default:i.vertexAttrib1fv(W.location,ce)}}}}y()}function N(){I();for(const g in n){const E=n[g];for(const k in E){const G=E[k];for(const X in G)d(G[X].object),delete G[X];delete E[k]}delete n[g]}}function R(g){if(n[g.id]===void 0)return;const E=n[g.id];for(const k in E){const G=E[k];for(const X in G)d(G[X].object),delete G[X];delete E[k]}delete n[g.id]}function w(g){for(const E in n){const k=n[E];if(k[g.id]===void 0)continue;const G=k[g.id];for(const X in G)d(G[X].object),delete G[X];delete k[g.id]}}function I(){K(),a=!0,s!==r&&(s=r,c(s.object))}function K(){r.geometry=null,r.program=null,r.wireframe=!1}return{setup:o,reset:I,resetDefaultState:K,dispose:N,releaseStatesOfGeometry:R,releaseStatesOfProgram:w,initAttributes:M,enableAttribute:h,disableUnusedAttributes:y}}function gd(i,e,t){let n;function r(c){n=c}function s(c,d){i.drawArrays(n,c,d),t.update(d,n,1)}function a(c,d,p){p!==0&&(i.drawArraysInstanced(n,c,d,p),t.update(d,n,p))}function o(c,d,p){if(p===0)return;e.get("WEBGL_multi_draw").multiDrawArraysWEBGL(n,c,0,d,0,p);let m=0;for(let _=0;_<p;_++)m+=d[_];t.update(m,n,1)}function l(c,d,p,f){if(p===0)return;const m=e.get("WEBGL_multi_draw");if(m===null)for(let _=0;_<c.length;_++)a(c[_],d[_],f[_]);else{m.multiDrawArraysInstancedWEBGL(n,c,0,d,0,f,0,p);let _=0;for(let M=0;M<p;M++)_+=d[M];for(let M=0;M<f.length;M++)t.update(_,n,f[M])}}this.setMode=r,this.render=s,this.renderInstances=a,this.renderMultiDraw=o,this.renderMultiDrawInstances=l}function vd(i,e,t,n){let r;function s(){if(r!==void 0)return r;if(e.has("EXT_texture_filter_anisotropic")===!0){const w=e.get("EXT_texture_filter_anisotropic");r=i.getParameter(w.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else r=0;return r}function a(w){return!(w!==Ht&&n.convert(w)!==i.getParameter(i.IMPLEMENTATION_COLOR_READ_FORMAT))}function o(w){const I=w===xi&&(e.has("EXT_color_buffer_half_float")||e.has("EXT_color_buffer_float"));return!(w!==nn&&n.convert(w)!==i.getParameter(i.IMPLEMENTATION_COLOR_READ_TYPE)&&w!==en&&!I)}function l(w){if(w==="highp"){if(i.getShaderPrecisionFormat(i.VERTEX_SHADER,i.HIGH_FLOAT).precision>0&&i.getShaderPrecisionFormat(i.FRAGMENT_SHADER,i.HIGH_FLOAT).precision>0)return"highp";w="mediump"}return w==="mediump"&&i.getShaderPrecisionFormat(i.VERTEX_SHADER,i.MEDIUM_FLOAT).precision>0&&i.getShaderPrecisionFormat(i.FRAGMENT_SHADER,i.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let c=t.precision!==void 0?t.precision:"highp";const d=l(c);d!==c&&(console.warn("THREE.WebGLRenderer:",c,"not supported, using",d,"instead."),c=d);const p=t.logarithmicDepthBuffer===!0,f=t.reverseDepthBuffer===!0&&e.has("EXT_clip_control");if(f===!0){const w=e.get("EXT_clip_control");w.clipControlEXT(w.LOWER_LEFT_EXT,w.ZERO_TO_ONE_EXT)}const m=i.getParameter(i.MAX_TEXTURE_IMAGE_UNITS),_=i.getParameter(i.MAX_VERTEX_TEXTURE_IMAGE_UNITS),M=i.getParameter(i.MAX_TEXTURE_SIZE),h=i.getParameter(i.MAX_CUBE_MAP_TEXTURE_SIZE),u=i.getParameter(i.MAX_VERTEX_ATTRIBS),y=i.getParameter(i.MAX_VERTEX_UNIFORM_VECTORS),S=i.getParameter(i.MAX_VARYING_VECTORS),T=i.getParameter(i.MAX_FRAGMENT_UNIFORM_VECTORS),N=_>0,R=i.getParameter(i.MAX_SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:s,getMaxPrecision:l,textureFormatReadable:a,textureTypeReadable:o,precision:c,logarithmicDepthBuffer:p,reverseDepthBuffer:f,maxTextures:m,maxVertexTextures:_,maxTextureSize:M,maxCubemapSize:h,maxAttributes:u,maxVertexUniforms:y,maxVaryings:S,maxFragmentUniforms:T,vertexTextures:N,maxSamples:R}}function xd(i){const e=this;let t=null,n=0,r=!1,s=!1;const a=new Tn,o=new De,l={value:null,needsUpdate:!1};this.uniform=l,this.numPlanes=0,this.numIntersection=0,this.init=function(p,f){const m=p.length!==0||f||n!==0||r;return r=f,n=p.length,m},this.beginShadows=function(){s=!0,d(null)},this.endShadows=function(){s=!1},this.setGlobalState=function(p,f){t=d(p,f,0)},this.setState=function(p,f,m){const _=p.clippingPlanes,M=p.clipIntersection,h=p.clipShadows,u=i.get(p);if(!r||_===null||_.length===0||s&&!h)s?d(null):c();else{const y=s?0:n,S=y*4;let T=u.clippingState||null;l.value=T,T=d(_,f,S,m);for(let N=0;N!==S;++N)T[N]=t[N];u.clippingState=T,this.numIntersection=M?this.numPlanes:0,this.numPlanes+=y}};function c(){l.value!==t&&(l.value=t,l.needsUpdate=n>0),e.numPlanes=n,e.numIntersection=0}function d(p,f,m,_){const M=p!==null?p.length:0;let h=null;if(M!==0){if(h=l.value,_!==!0||h===null){const u=m+M*4,y=f.matrixWorldInverse;o.getNormalMatrix(y),(h===null||h.length<u)&&(h=new Float32Array(u));for(let S=0,T=m;S!==M;++S,T+=4)a.copy(p[S]).applyMatrix4(y,o),a.normal.toArray(h,T),h[T+3]=a.constant}l.value=h,l.needsUpdate=!0}return e.numPlanes=M,e.numIntersection=0,h}}function Md(i){let e=new WeakMap;function t(a,o){return o===fs?a.mapping=Jn:o===ps&&(a.mapping=Qn),a}function n(a){if(a&&a.isTexture){const o=a.mapping;if(o===fs||o===ps)if(e.has(a)){const l=e.get(a).texture;return t(l,a.mapping)}else{const l=a.image;if(l&&l.height>0){const c=new Lc(l.height);return c.fromEquirectangularTexture(i,a),e.set(a,c),a.addEventListener("dispose",r),t(c.texture,a.mapping)}else return null}}return a}function r(a){const o=a.target;o.removeEventListener("dispose",r);const l=e.get(o);l!==void 0&&(e.delete(o),l.dispose())}function s(){e=new WeakMap}return{get:n,dispose:s}}class Xo extends Vo{constructor(e=-1,t=1,n=1,r=-1,s=.1,a=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=e,this.right=t,this.top=n,this.bottom=r,this.near=s,this.far=a,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.left=e.left,this.right=e.right,this.top=e.top,this.bottom=e.bottom,this.near=e.near,this.far=e.far,this.zoom=e.zoom,this.view=e.view===null?null:Object.assign({},e.view),this}setViewOffset(e,t,n,r,s,a){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=r,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=(this.right-this.left)/(2*this.zoom),t=(this.top-this.bottom)/(2*this.zoom),n=(this.right+this.left)/2,r=(this.top+this.bottom)/2;let s=n-e,a=n+e,o=r+t,l=r-t;if(this.view!==null&&this.view.enabled){const c=(this.right-this.left)/this.view.fullWidth/this.zoom,d=(this.top-this.bottom)/this.view.fullHeight/this.zoom;s+=c*this.view.offsetX,a=s+c*this.view.width,o-=d*this.view.offsetY,l=o-d*this.view.height}this.projectionMatrix.makeOrthographic(s,a,o,l,this.near,this.far,this.coordinateSystem),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.zoom=this.zoom,t.object.left=this.left,t.object.right=this.right,t.object.top=this.top,t.object.bottom=this.bottom,t.object.near=this.near,t.object.far=this.far,this.view!==null&&(t.object.view=Object.assign({},this.view)),t}}const Yn=4,za=[.125,.215,.35,.446,.526,.582],wn=20,Kr=new Xo,Ha=new Oe;let $r=null,Zr=0,jr=0,Jr=!1;const An=(1+Math.sqrt(5))/2,qn=1/An,Ga=[new D(-An,qn,0),new D(An,qn,0),new D(-qn,0,An),new D(qn,0,An),new D(0,An,-qn),new D(0,An,qn),new D(-1,1,-1),new D(1,1,-1),new D(-1,1,1),new D(1,1,1)];class Va{constructor(e){this._renderer=e,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._lodPlanes=[],this._sizeLods=[],this._sigmas=[],this._blurMaterial=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._compileMaterial(this._blurMaterial)}fromScene(e,t=0,n=.1,r=100){$r=this._renderer.getRenderTarget(),Zr=this._renderer.getActiveCubeFace(),jr=this._renderer.getActiveMipmapLevel(),Jr=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(256);const s=this._allocateTargets();return s.depthBuffer=!0,this._sceneToCubeUV(e,n,r,s),t>0&&this._blur(s,0,0,t),this._applyPMREM(s),this._cleanup(s),s}fromEquirectangular(e,t=null){return this._fromTexture(e,t)}fromCubemap(e,t=null){return this._fromTexture(e,t)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=Xa(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=Wa(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose()}_setSize(e){this._lodMax=Math.floor(Math.log2(e)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let e=0;e<this._lodPlanes.length;e++)this._lodPlanes[e].dispose()}_cleanup(e){this._renderer.setRenderTarget($r,Zr,jr),this._renderer.xr.enabled=Jr,e.scissorTest=!1,Yi(e,0,0,e.width,e.height)}_fromTexture(e,t){e.mapping===Jn||e.mapping===Qn?this._setSize(e.image.length===0?16:e.image[0].width||e.image[0].image.width):this._setSize(e.image.width/4),$r=this._renderer.getRenderTarget(),Zr=this._renderer.getActiveCubeFace(),jr=this._renderer.getActiveMipmapLevel(),Jr=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const n=t||this._allocateTargets();return this._textureToCubeUV(e,n),this._applyPMREM(n),this._cleanup(n),n}_allocateTargets(){const e=3*Math.max(this._cubeSize,112),t=4*this._cubeSize,n={magFilter:Bt,minFilter:Bt,generateMipmaps:!1,type:xi,format:Ht,colorSpace:_n,depthBuffer:!1},r=ka(e,t,n);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==e||this._pingPongRenderTarget.height!==t){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=ka(e,t,n);const{_lodMax:s}=this;({sizeLods:this._sizeLods,lodPlanes:this._lodPlanes,sigmas:this._sigmas}=Sd(s)),this._blurMaterial=Ed(s,e,t)}return r}_compileMaterial(e){const t=new wt(this._lodPlanes[0],e);this._renderer.compile(t,Kr)}_sceneToCubeUV(e,t,n,r){const o=new bt(90,1,t,n),l=[1,-1,1,1,1,1],c=[1,1,1,-1,-1,-1],d=this._renderer,p=d.autoClear,f=d.toneMapping;d.getClearColor(Ha),d.toneMapping=fn,d.autoClear=!1;const m=new cr({name:"PMREM.Background",side:Mt,depthWrite:!1,depthTest:!1}),_=new wt(new yi,m);let M=!1;const h=e.background;h?h.isColor&&(m.color.copy(h),e.background=null,M=!0):(m.color.copy(Ha),M=!0);for(let u=0;u<6;u++){const y=u%3;y===0?(o.up.set(0,l[u],0),o.lookAt(c[u],0,0)):y===1?(o.up.set(0,0,l[u]),o.lookAt(0,c[u],0)):(o.up.set(0,l[u],0),o.lookAt(0,0,c[u]));const S=this._cubeSize;Yi(r,y*S,u>2?S:0,S,S),d.setRenderTarget(r),M&&d.render(_,o),d.render(e,o)}_.geometry.dispose(),_.material.dispose(),d.toneMapping=f,d.autoClear=p,e.background=h}_textureToCubeUV(e,t){const n=this._renderer,r=e.mapping===Jn||e.mapping===Qn;r?(this._cubemapMaterial===null&&(this._cubemapMaterial=Xa()),this._cubemapMaterial.uniforms.flipEnvMap.value=e.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=Wa());const s=r?this._cubemapMaterial:this._equirectMaterial,a=new wt(this._lodPlanes[0],s),o=s.uniforms;o.envMap.value=e;const l=this._cubeSize;Yi(t,0,0,3*l,2*l),n.setRenderTarget(t),n.render(a,Kr)}_applyPMREM(e){const t=this._renderer,n=t.autoClear;t.autoClear=!1;const r=this._lodPlanes.length;for(let s=1;s<r;s++){const a=Math.sqrt(this._sigmas[s]*this._sigmas[s]-this._sigmas[s-1]*this._sigmas[s-1]),o=Ga[(r-s-1)%Ga.length];this._blur(e,s-1,s,a,o)}t.autoClear=n}_blur(e,t,n,r,s){const a=this._pingPongRenderTarget;this._halfBlur(e,a,t,n,r,"latitudinal",s),this._halfBlur(a,e,n,n,r,"longitudinal",s)}_halfBlur(e,t,n,r,s,a,o){const l=this._renderer,c=this._blurMaterial;a!=="latitudinal"&&a!=="longitudinal"&&console.error("blur direction must be either latitudinal or longitudinal!");const d=3,p=new wt(this._lodPlanes[r],c),f=c.uniforms,m=this._sizeLods[n]-1,_=isFinite(s)?Math.PI/(2*m):2*Math.PI/(2*wn-1),M=s/_,h=isFinite(s)?1+Math.floor(d*M):wn;h>wn&&console.warn(`sigmaRadians, ${s}, is too large and will clip, as it requested ${h} samples when the maximum is set to ${wn}`);const u=[];let y=0;for(let w=0;w<wn;++w){const I=w/M,K=Math.exp(-I*I/2);u.push(K),w===0?y+=K:w<h&&(y+=2*K)}for(let w=0;w<u.length;w++)u[w]=u[w]/y;f.envMap.value=e.texture,f.samples.value=h,f.weights.value=u,f.latitudinal.value=a==="latitudinal",o&&(f.poleAxis.value=o);const{_lodMax:S}=this;f.dTheta.value=_,f.mipInt.value=S-n;const T=this._sizeLods[r],N=3*T*(r>S-Yn?r-S+Yn:0),R=4*(this._cubeSize-T);Yi(t,N,R,3*T,2*T),l.setRenderTarget(t),l.render(p,Kr)}}function Sd(i){const e=[],t=[],n=[];let r=i;const s=i-Yn+1+za.length;for(let a=0;a<s;a++){const o=Math.pow(2,r);t.push(o);let l=1/o;a>i-Yn?l=za[a-i+Yn-1]:a===0&&(l=0),n.push(l);const c=1/(o-2),d=-c,p=1+c,f=[d,d,p,d,p,p,d,d,p,p,d,p],m=6,_=6,M=3,h=2,u=1,y=new Float32Array(M*_*m),S=new Float32Array(h*_*m),T=new Float32Array(u*_*m);for(let R=0;R<m;R++){const w=R%3*2/3-1,I=R>2?0:-1,K=[w,I,0,w+2/3,I,0,w+2/3,I+1,0,w,I,0,w+2/3,I+1,0,w,I+1,0];y.set(K,M*_*R),S.set(f,h*_*R);const g=[R,R,R,R,R,R];T.set(g,u*_*R)}const N=new Rt;N.setAttribute("position",new Wt(y,M)),N.setAttribute("uv",new Wt(S,h)),N.setAttribute("faceIndex",new Wt(T,u)),e.push(N),r>Yn&&r--}return{lodPlanes:e,sizeLods:t,sigmas:n}}function ka(i,e,t){const n=new Ln(i,e,t);return n.texture.mapping=fr,n.texture.name="PMREM.cubeUv",n.scissorTest=!0,n}function Yi(i,e,t,n,r){i.viewport.set(e,t,n,r),i.scissor.set(e,t,n,r)}function Ed(i,e,t){const n=new Float32Array(wn),r=new D(0,1,0);return new mn({name:"SphericalGaussianBlur",defines:{n:wn,CUBEUV_TEXEL_WIDTH:1/e,CUBEUV_TEXEL_HEIGHT:1/t,CUBEUV_MAX_MIP:`${i}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:n},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:r}},vertexShader:ea(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;
			uniform int samples;
			uniform float weights[ n ];
			uniform bool latitudinal;
			uniform float dTheta;
			uniform float mipInt;
			uniform vec3 poleAxis;

			#define ENVMAP_TYPE_CUBE_UV
			#include <cube_uv_reflection_fragment>

			vec3 getSample( float theta, vec3 axis ) {

				float cosTheta = cos( theta );
				// Rodrigues' axis-angle rotation
				vec3 sampleDirection = vOutputDirection * cosTheta
					+ cross( axis, vOutputDirection ) * sin( theta )
					+ axis * dot( axis, vOutputDirection ) * ( 1.0 - cosTheta );

				return bilinearCubeUV( envMap, sampleDirection, mipInt );

			}

			void main() {

				vec3 axis = latitudinal ? poleAxis : cross( poleAxis, vOutputDirection );

				if ( all( equal( axis, vec3( 0.0 ) ) ) ) {

					axis = vec3( vOutputDirection.z, 0.0, - vOutputDirection.x );

				}

				axis = normalize( axis );

				gl_FragColor = vec4( 0.0, 0.0, 0.0, 1.0 );
				gl_FragColor.rgb += weights[ 0 ] * getSample( 0.0, axis );

				for ( int i = 1; i < n; i++ ) {

					if ( i >= samples ) {

						break;

					}

					float theta = dTheta * float( i );
					gl_FragColor.rgb += weights[ i ] * getSample( -1.0 * theta, axis );
					gl_FragColor.rgb += weights[ i ] * getSample( theta, axis );

				}

			}
		`,blending:dn,depthTest:!1,depthWrite:!1})}function Wa(){return new mn({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:ea(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;

			#include <common>

			void main() {

				vec3 outputDirection = normalize( vOutputDirection );
				vec2 uv = equirectUv( outputDirection );

				gl_FragColor = vec4( texture2D ( envMap, uv ).rgb, 1.0 );

			}
		`,blending:dn,depthTest:!1,depthWrite:!1})}function Xa(){return new mn({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:ea(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:dn,depthTest:!1,depthWrite:!1})}function ea(){return`

		precision mediump float;
		precision mediump int;

		attribute float faceIndex;

		varying vec3 vOutputDirection;

		// RH coordinate system; PMREM face-indexing convention
		vec3 getDirection( vec2 uv, float face ) {

			uv = 2.0 * uv - 1.0;

			vec3 direction = vec3( uv, 1.0 );

			if ( face == 0.0 ) {

				direction = direction.zyx; // ( 1, v, u ) pos x

			} else if ( face == 1.0 ) {

				direction = direction.xzy;
				direction.xz *= -1.0; // ( -u, 1, -v ) pos y

			} else if ( face == 2.0 ) {

				direction.x *= -1.0; // ( -u, v, 1 ) pos z

			} else if ( face == 3.0 ) {

				direction = direction.zyx;
				direction.xz *= -1.0; // ( -1, v, -u ) neg x

			} else if ( face == 4.0 ) {

				direction = direction.xzy;
				direction.xy *= -1.0; // ( -u, -1, v ) neg y

			} else if ( face == 5.0 ) {

				direction.z *= -1.0; // ( u, v, -1 ) neg z

			}

			return direction;

		}

		void main() {

			vOutputDirection = getDirection( uv, faceIndex );
			gl_Position = vec4( position, 1.0 );

		}
	`}function yd(i){let e=new WeakMap,t=null;function n(o){if(o&&o.isTexture){const l=o.mapping,c=l===fs||l===ps,d=l===Jn||l===Qn;if(c||d){let p=e.get(o);const f=p!==void 0?p.texture.pmremVersion:0;if(o.isRenderTargetTexture&&o.pmremVersion!==f)return t===null&&(t=new Va(i)),p=c?t.fromEquirectangular(o,p):t.fromCubemap(o,p),p.texture.pmremVersion=o.pmremVersion,e.set(o,p),p.texture;if(p!==void 0)return p.texture;{const m=o.image;return c&&m&&m.height>0||d&&m&&r(m)?(t===null&&(t=new Va(i)),p=c?t.fromEquirectangular(o):t.fromCubemap(o),p.texture.pmremVersion=o.pmremVersion,e.set(o,p),o.addEventListener("dispose",s),p.texture):null}}}return o}function r(o){let l=0;const c=6;for(let d=0;d<c;d++)o[d]!==void 0&&l++;return l===c}function s(o){const l=o.target;l.removeEventListener("dispose",s);const c=e.get(l);c!==void 0&&(e.delete(l),c.dispose())}function a(){e=new WeakMap,t!==null&&(t.dispose(),t=null)}return{get:n,dispose:a}}function Td(i){const e={};function t(n){if(e[n]!==void 0)return e[n];let r;switch(n){case"WEBGL_depth_texture":r=i.getExtension("WEBGL_depth_texture")||i.getExtension("MOZ_WEBGL_depth_texture")||i.getExtension("WEBKIT_WEBGL_depth_texture");break;case"EXT_texture_filter_anisotropic":r=i.getExtension("EXT_texture_filter_anisotropic")||i.getExtension("MOZ_EXT_texture_filter_anisotropic")||i.getExtension("WEBKIT_EXT_texture_filter_anisotropic");break;case"WEBGL_compressed_texture_s3tc":r=i.getExtension("WEBGL_compressed_texture_s3tc")||i.getExtension("MOZ_WEBGL_compressed_texture_s3tc")||i.getExtension("WEBKIT_WEBGL_compressed_texture_s3tc");break;case"WEBGL_compressed_texture_pvrtc":r=i.getExtension("WEBGL_compressed_texture_pvrtc")||i.getExtension("WEBKIT_WEBGL_compressed_texture_pvrtc");break;default:r=i.getExtension(n)}return e[n]=r,r}return{has:function(n){return t(n)!==null},init:function(){t("EXT_color_buffer_float"),t("WEBGL_clip_cull_distance"),t("OES_texture_float_linear"),t("EXT_color_buffer_half_float"),t("WEBGL_multisampled_render_to_texture"),t("WEBGL_render_shared_exponent")},get:function(n){const r=t(n);return r===null&&nr("THREE.WebGLRenderer: "+n+" extension not supported."),r}}}function Ad(i,e,t,n){const r={},s=new WeakMap;function a(p){const f=p.target;f.index!==null&&e.remove(f.index);for(const _ in f.attributes)e.remove(f.attributes[_]);for(const _ in f.morphAttributes){const M=f.morphAttributes[_];for(let h=0,u=M.length;h<u;h++)e.remove(M[h])}f.removeEventListener("dispose",a),delete r[f.id];const m=s.get(f);m&&(e.remove(m),s.delete(f)),n.releaseStatesOfGeometry(f),f.isInstancedBufferGeometry===!0&&delete f._maxInstanceCount,t.memory.geometries--}function o(p,f){return r[f.id]===!0||(f.addEventListener("dispose",a),r[f.id]=!0,t.memory.geometries++),f}function l(p){const f=p.attributes;for(const _ in f)e.update(f[_],i.ARRAY_BUFFER);const m=p.morphAttributes;for(const _ in m){const M=m[_];for(let h=0,u=M.length;h<u;h++)e.update(M[h],i.ARRAY_BUFFER)}}function c(p){const f=[],m=p.index,_=p.attributes.position;let M=0;if(m!==null){const y=m.array;M=m.version;for(let S=0,T=y.length;S<T;S+=3){const N=y[S+0],R=y[S+1],w=y[S+2];f.push(N,R,R,w,w,N)}}else if(_!==void 0){const y=_.array;M=_.version;for(let S=0,T=y.length/3-1;S<T;S+=3){const N=S+0,R=S+1,w=S+2;f.push(N,R,R,w,w,N)}}else return;const h=new(Uo(f)?Ho:zo)(f,1);h.version=M;const u=s.get(p);u&&e.remove(u),s.set(p,h)}function d(p){const f=s.get(p);if(f){const m=p.index;m!==null&&f.version<m.version&&c(p)}else c(p);return s.get(p)}return{get:o,update:l,getWireframeAttribute:d}}function bd(i,e,t){let n;function r(f){n=f}let s,a;function o(f){s=f.type,a=f.bytesPerElement}function l(f,m){i.drawElements(n,m,s,f*a),t.update(m,n,1)}function c(f,m,_){_!==0&&(i.drawElementsInstanced(n,m,s,f*a,_),t.update(m,n,_))}function d(f,m,_){if(_===0)return;e.get("WEBGL_multi_draw").multiDrawElementsWEBGL(n,m,0,s,f,0,_);let h=0;for(let u=0;u<_;u++)h+=m[u];t.update(h,n,1)}function p(f,m,_,M){if(_===0)return;const h=e.get("WEBGL_multi_draw");if(h===null)for(let u=0;u<f.length;u++)c(f[u]/a,m[u],M[u]);else{h.multiDrawElementsInstancedWEBGL(n,m,0,s,f,0,M,0,_);let u=0;for(let y=0;y<_;y++)u+=m[y];for(let y=0;y<M.length;y++)t.update(u,n,M[y])}}this.setMode=r,this.setIndex=o,this.render=l,this.renderInstances=c,this.renderMultiDraw=d,this.renderMultiDrawInstances=p}function wd(i){const e={geometries:0,textures:0},t={frame:0,calls:0,triangles:0,points:0,lines:0};function n(s,a,o){switch(t.calls++,a){case i.TRIANGLES:t.triangles+=o*(s/3);break;case i.LINES:t.lines+=o*(s/2);break;case i.LINE_STRIP:t.lines+=o*(s-1);break;case i.LINE_LOOP:t.lines+=o*s;break;case i.POINTS:t.points+=o*s;break;default:console.error("THREE.WebGLInfo: Unknown draw mode:",a);break}}function r(){t.calls=0,t.triangles=0,t.points=0,t.lines=0}return{memory:e,render:t,programs:null,autoReset:!0,reset:r,update:n}}function Rd(i,e,t){const n=new WeakMap,r=new Ze;function s(a,o,l){const c=a.morphTargetInfluences,d=o.morphAttributes.position||o.morphAttributes.normal||o.morphAttributes.color,p=d!==void 0?d.length:0;let f=n.get(o);if(f===void 0||f.count!==p){let K=function(){w.dispose(),n.delete(o),o.removeEventListener("dispose",K)};f!==void 0&&f.texture.dispose();const m=o.morphAttributes.position!==void 0,_=o.morphAttributes.normal!==void 0,M=o.morphAttributes.color!==void 0,h=o.morphAttributes.position||[],u=o.morphAttributes.normal||[],y=o.morphAttributes.color||[];let S=0;m===!0&&(S=1),_===!0&&(S=2),M===!0&&(S=3);let T=o.attributes.position.count*S,N=1;T>e.maxTextureSize&&(N=Math.ceil(T/e.maxTextureSize),T=e.maxTextureSize);const R=new Float32Array(T*N*4*p),w=new No(R,T,N,p);w.type=en,w.needsUpdate=!0;const I=S*4;for(let g=0;g<p;g++){const E=h[g],k=u[g],G=y[g],X=T*N*4*g;for(let J=0;J<E.count;J++){const H=J*I;m===!0&&(r.fromBufferAttribute(E,J),R[X+H+0]=r.x,R[X+H+1]=r.y,R[X+H+2]=r.z,R[X+H+3]=0),_===!0&&(r.fromBufferAttribute(k,J),R[X+H+4]=r.x,R[X+H+5]=r.y,R[X+H+6]=r.z,R[X+H+7]=0),M===!0&&(r.fromBufferAttribute(G,J),R[X+H+8]=r.x,R[X+H+9]=r.y,R[X+H+10]=r.z,R[X+H+11]=G.itemSize===4?r.w:1)}}f={count:p,texture:w,size:new ze(T,N)},n.set(o,f),o.addEventListener("dispose",K)}if(a.isInstancedMesh===!0&&a.morphTexture!==null)l.getUniforms().setValue(i,"morphTexture",a.morphTexture,t);else{let m=0;for(let M=0;M<c.length;M++)m+=c[M];const _=o.morphTargetsRelative?1:1-m;l.getUniforms().setValue(i,"morphTargetBaseInfluence",_),l.getUniforms().setValue(i,"morphTargetInfluences",c)}l.getUniforms().setValue(i,"morphTargetsTexture",f.texture,t),l.getUniforms().setValue(i,"morphTargetsTextureSize",f.size)}return{update:s}}function Cd(i,e,t,n){let r=new WeakMap;function s(l){const c=n.render.frame,d=l.geometry,p=e.get(l,d);if(r.get(p)!==c&&(e.update(p),r.set(p,c)),l.isInstancedMesh&&(l.hasEventListener("dispose",o)===!1&&l.addEventListener("dispose",o),r.get(l)!==c&&(t.update(l.instanceMatrix,i.ARRAY_BUFFER),l.instanceColor!==null&&t.update(l.instanceColor,i.ARRAY_BUFFER),r.set(l,c))),l.isSkinnedMesh){const f=l.skeleton;r.get(f)!==c&&(f.update(),r.set(f,c))}return p}function a(){r=new WeakMap}function o(l){const c=l.target;c.removeEventListener("dispose",o),t.remove(c.instanceMatrix),c.instanceColor!==null&&t.remove(c.instanceColor)}return{update:s,dispose:a}}class qo extends St{constructor(e,t,n,r,s,a,o,l,c,d=$n){if(d!==$n&&d!==ti)throw new Error("DepthTexture format must be either THREE.DepthFormat or THREE.DepthStencilFormat");n===void 0&&d===$n&&(n=Pn),n===void 0&&d===ti&&(n=ei),super(null,r,s,a,o,l,d,n,c),this.isDepthTexture=!0,this.image={width:e,height:t},this.magFilter=o!==void 0?o:Lt,this.minFilter=l!==void 0?l:Lt,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(e){return super.copy(e),this.compareFunction=e.compareFunction,this}toJSON(e){const t=super.toJSON(e);return this.compareFunction!==null&&(t.compareFunction=this.compareFunction),t}}const Yo=new St,qa=new qo(1,1),Ko=new No,$o=new mc,Zo=new ko,Ya=[],Ka=[],$a=new Float32Array(16),Za=new Float32Array(9),ja=new Float32Array(4);function si(i,e,t){const n=i[0];if(n<=0||n>0)return i;const r=e*t;let s=Ya[r];if(s===void 0&&(s=new Float32Array(r),Ya[r]=s),e!==0){n.toArray(s,0);for(let a=1,o=0;a!==e;++a)o+=t,i[a].toArray(s,o)}return s}function ot(i,e){if(i.length!==e.length)return!1;for(let t=0,n=i.length;t<n;t++)if(i[t]!==e[t])return!1;return!0}function lt(i,e){for(let t=0,n=e.length;t<n;t++)i[t]=e[t]}function gr(i,e){let t=Ka[e];t===void 0&&(t=new Int32Array(e),Ka[e]=t);for(let n=0;n!==e;++n)t[n]=i.allocateTextureUnit();return t}function Pd(i,e){const t=this.cache;t[0]!==e&&(i.uniform1f(this.addr,e),t[0]=e)}function Ld(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(i.uniform2f(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(ot(t,e))return;i.uniform2fv(this.addr,e),lt(t,e)}}function Dd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(i.uniform3f(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else if(e.r!==void 0)(t[0]!==e.r||t[1]!==e.g||t[2]!==e.b)&&(i.uniform3f(this.addr,e.r,e.g,e.b),t[0]=e.r,t[1]=e.g,t[2]=e.b);else{if(ot(t,e))return;i.uniform3fv(this.addr,e),lt(t,e)}}function Ud(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(i.uniform4f(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(ot(t,e))return;i.uniform4fv(this.addr,e),lt(t,e)}}function Id(i,e){const t=this.cache,n=e.elements;if(n===void 0){if(ot(t,e))return;i.uniformMatrix2fv(this.addr,!1,e),lt(t,e)}else{if(ot(t,n))return;ja.set(n),i.uniformMatrix2fv(this.addr,!1,ja),lt(t,n)}}function Nd(i,e){const t=this.cache,n=e.elements;if(n===void 0){if(ot(t,e))return;i.uniformMatrix3fv(this.addr,!1,e),lt(t,e)}else{if(ot(t,n))return;Za.set(n),i.uniformMatrix3fv(this.addr,!1,Za),lt(t,n)}}function Fd(i,e){const t=this.cache,n=e.elements;if(n===void 0){if(ot(t,e))return;i.uniformMatrix4fv(this.addr,!1,e),lt(t,e)}else{if(ot(t,n))return;$a.set(n),i.uniformMatrix4fv(this.addr,!1,$a),lt(t,n)}}function Od(i,e){const t=this.cache;t[0]!==e&&(i.uniform1i(this.addr,e),t[0]=e)}function Bd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(i.uniform2i(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(ot(t,e))return;i.uniform2iv(this.addr,e),lt(t,e)}}function zd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(i.uniform3i(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(ot(t,e))return;i.uniform3iv(this.addr,e),lt(t,e)}}function Hd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(i.uniform4i(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(ot(t,e))return;i.uniform4iv(this.addr,e),lt(t,e)}}function Gd(i,e){const t=this.cache;t[0]!==e&&(i.uniform1ui(this.addr,e),t[0]=e)}function Vd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(i.uniform2ui(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(ot(t,e))return;i.uniform2uiv(this.addr,e),lt(t,e)}}function kd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(i.uniform3ui(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(ot(t,e))return;i.uniform3uiv(this.addr,e),lt(t,e)}}function Wd(i,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(i.uniform4ui(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(ot(t,e))return;i.uniform4uiv(this.addr,e),lt(t,e)}}function Xd(i,e,t){const n=this.cache,r=t.allocateTextureUnit();n[0]!==r&&(i.uniform1i(this.addr,r),n[0]=r);let s;this.type===i.SAMPLER_2D_SHADOW?(qa.compareFunction=Do,s=qa):s=Yo,t.setTexture2D(e||s,r)}function qd(i,e,t){const n=this.cache,r=t.allocateTextureUnit();n[0]!==r&&(i.uniform1i(this.addr,r),n[0]=r),t.setTexture3D(e||$o,r)}function Yd(i,e,t){const n=this.cache,r=t.allocateTextureUnit();n[0]!==r&&(i.uniform1i(this.addr,r),n[0]=r),t.setTextureCube(e||Zo,r)}function Kd(i,e,t){const n=this.cache,r=t.allocateTextureUnit();n[0]!==r&&(i.uniform1i(this.addr,r),n[0]=r),t.setTexture2DArray(e||Ko,r)}function $d(i){switch(i){case 5126:return Pd;case 35664:return Ld;case 35665:return Dd;case 35666:return Ud;case 35674:return Id;case 35675:return Nd;case 35676:return Fd;case 5124:case 35670:return Od;case 35667:case 35671:return Bd;case 35668:case 35672:return zd;case 35669:case 35673:return Hd;case 5125:return Gd;case 36294:return Vd;case 36295:return kd;case 36296:return Wd;case 35678:case 36198:case 36298:case 36306:case 35682:return Xd;case 35679:case 36299:case 36307:return qd;case 35680:case 36300:case 36308:case 36293:return Yd;case 36289:case 36303:case 36311:case 36292:return Kd}}function Zd(i,e){i.uniform1fv(this.addr,e)}function jd(i,e){const t=si(e,this.size,2);i.uniform2fv(this.addr,t)}function Jd(i,e){const t=si(e,this.size,3);i.uniform3fv(this.addr,t)}function Qd(i,e){const t=si(e,this.size,4);i.uniform4fv(this.addr,t)}function ef(i,e){const t=si(e,this.size,4);i.uniformMatrix2fv(this.addr,!1,t)}function tf(i,e){const t=si(e,this.size,9);i.uniformMatrix3fv(this.addr,!1,t)}function nf(i,e){const t=si(e,this.size,16);i.uniformMatrix4fv(this.addr,!1,t)}function rf(i,e){i.uniform1iv(this.addr,e)}function sf(i,e){i.uniform2iv(this.addr,e)}function af(i,e){i.uniform3iv(this.addr,e)}function of(i,e){i.uniform4iv(this.addr,e)}function lf(i,e){i.uniform1uiv(this.addr,e)}function cf(i,e){i.uniform2uiv(this.addr,e)}function uf(i,e){i.uniform3uiv(this.addr,e)}function hf(i,e){i.uniform4uiv(this.addr,e)}function df(i,e,t){const n=this.cache,r=e.length,s=gr(t,r);ot(n,s)||(i.uniform1iv(this.addr,s),lt(n,s));for(let a=0;a!==r;++a)t.setTexture2D(e[a]||Yo,s[a])}function ff(i,e,t){const n=this.cache,r=e.length,s=gr(t,r);ot(n,s)||(i.uniform1iv(this.addr,s),lt(n,s));for(let a=0;a!==r;++a)t.setTexture3D(e[a]||$o,s[a])}function pf(i,e,t){const n=this.cache,r=e.length,s=gr(t,r);ot(n,s)||(i.uniform1iv(this.addr,s),lt(n,s));for(let a=0;a!==r;++a)t.setTextureCube(e[a]||Zo,s[a])}function mf(i,e,t){const n=this.cache,r=e.length,s=gr(t,r);ot(n,s)||(i.uniform1iv(this.addr,s),lt(n,s));for(let a=0;a!==r;++a)t.setTexture2DArray(e[a]||Ko,s[a])}function _f(i){switch(i){case 5126:return Zd;case 35664:return jd;case 35665:return Jd;case 35666:return Qd;case 35674:return ef;case 35675:return tf;case 35676:return nf;case 5124:case 35670:return rf;case 35667:case 35671:return sf;case 35668:case 35672:return af;case 35669:case 35673:return of;case 5125:return lf;case 36294:return cf;case 36295:return uf;case 36296:return hf;case 35678:case 36198:case 36298:case 36306:case 35682:return df;case 35679:case 36299:case 36307:return ff;case 35680:case 36300:case 36308:case 36293:return pf;case 36289:case 36303:case 36311:case 36292:return mf}}class gf{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.setValue=$d(t.type)}}class vf{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.size=t.size,this.setValue=_f(t.type)}}class xf{constructor(e){this.id=e,this.seq=[],this.map={}}setValue(e,t,n){const r=this.seq;for(let s=0,a=r.length;s!==a;++s){const o=r[s];o.setValue(e,t[o.id],n)}}}const Qr=/(\w+)(\])?(\[|\.)?/g;function Ja(i,e){i.seq.push(e),i.map[e.id]=e}function Mf(i,e,t){const n=i.name,r=n.length;for(Qr.lastIndex=0;;){const s=Qr.exec(n),a=Qr.lastIndex;let o=s[1];const l=s[2]==="]",c=s[3];if(l&&(o=o|0),c===void 0||c==="["&&a+2===r){Ja(t,c===void 0?new gf(o,i,e):new vf(o,i,e));break}else{let p=t.map[o];p===void 0&&(p=new xf(o),Ja(t,p)),t=p}}}class ir{constructor(e,t){this.seq=[],this.map={};const n=e.getProgramParameter(t,e.ACTIVE_UNIFORMS);for(let r=0;r<n;++r){const s=e.getActiveUniform(t,r),a=e.getUniformLocation(t,s.name);Mf(s,a,this)}}setValue(e,t,n,r){const s=this.map[t];s!==void 0&&s.setValue(e,n,r)}setOptional(e,t,n){const r=t[n];r!==void 0&&this.setValue(e,n,r)}static upload(e,t,n,r){for(let s=0,a=t.length;s!==a;++s){const o=t[s],l=n[o.id];l.needsUpdate!==!1&&o.setValue(e,l.value,r)}}static seqWithValue(e,t){const n=[];for(let r=0,s=e.length;r!==s;++r){const a=e[r];a.id in t&&n.push(a)}return n}}function Qa(i,e,t){const n=i.createShader(e);return i.shaderSource(n,t),i.compileShader(n),n}const Sf=37297;let Ef=0;function yf(i,e){const t=i.split(`
`),n=[],r=Math.max(e-6,0),s=Math.min(e+6,t.length);for(let a=r;a<s;a++){const o=a+1;n.push(`${o===e?">":" "} ${o}: ${t[a]}`)}return n.join(`
`)}function Tf(i){const e=Ke.getPrimaries(Ke.workingColorSpace),t=Ke.getPrimaries(i);let n;switch(e===t?n="":e===ar&&t===sr?n="LinearDisplayP3ToLinearSRGB":e===sr&&t===ar&&(n="LinearSRGBToLinearDisplayP3"),i){case _n:case pr:return[n,"LinearTransferOETF"];case Vt:case Js:return[n,"sRGBTransferOETF"];default:return console.warn("THREE.WebGLProgram: Unsupported color space:",i),[n,"LinearTransferOETF"]}}function eo(i,e,t){const n=i.getShaderParameter(e,i.COMPILE_STATUS),r=i.getShaderInfoLog(e).trim();if(n&&r==="")return"";const s=/ERROR: 0:(\d+)/.exec(r);if(s){const a=parseInt(s[1]);return t.toUpperCase()+`

`+r+`

`+yf(i.getShaderSource(e),a)}else return r}function Af(i,e){const t=Tf(e);return`vec4 ${i}( vec4 value ) { return ${t[0]}( ${t[1]}( value ) ); }`}function bf(i,e){let t;switch(e){case Gl:t="Linear";break;case Vl:t="Reinhard";break;case kl:t="Cineon";break;case Wl:t="ACESFilmic";break;case ql:t="AgX";break;case Yl:t="Neutral";break;case Xl:t="Custom";break;default:console.warn("THREE.WebGLProgram: Unsupported toneMapping:",e),t="Linear"}return"vec3 "+i+"( vec3 color ) { return "+t+"ToneMapping( color ); }"}const Ki=new D;function wf(){Ke.getLuminanceCoefficients(Ki);const i=Ki.x.toFixed(4),e=Ki.y.toFixed(4),t=Ki.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${i}, ${e}, ${t} );`,"	return dot( weights, rgb );","}"].join(`
`)}function Rf(i){return[i.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",i.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(_i).join(`
`)}function Cf(i){const e=[];for(const t in i){const n=i[t];n!==!1&&e.push("#define "+t+" "+n)}return e.join(`
`)}function Pf(i,e){const t={},n=i.getProgramParameter(e,i.ACTIVE_ATTRIBUTES);for(let r=0;r<n;r++){const s=i.getActiveAttrib(e,r),a=s.name;let o=1;s.type===i.FLOAT_MAT2&&(o=2),s.type===i.FLOAT_MAT3&&(o=3),s.type===i.FLOAT_MAT4&&(o=4),t[a]={type:s.type,location:i.getAttribLocation(e,a),locationSize:o}}return t}function _i(i){return i!==""}function to(i,e){const t=e.numSpotLightShadows+e.numSpotLightMaps-e.numSpotLightShadowsWithMaps;return i.replace(/NUM_DIR_LIGHTS/g,e.numDirLights).replace(/NUM_SPOT_LIGHTS/g,e.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,e.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,t).replace(/NUM_RECT_AREA_LIGHTS/g,e.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,e.numPointLights).replace(/NUM_HEMI_LIGHTS/g,e.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,e.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,e.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,e.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,e.numPointLightShadows)}function no(i,e){return i.replace(/NUM_CLIPPING_PLANES/g,e.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,e.numClippingPlanes-e.numClipIntersection)}const Lf=/^[ \t]*#include +<([\w\d./]+)>/gm;function Ws(i){return i.replace(Lf,Uf)}const Df=new Map;function Uf(i,e){let t=Le[e];if(t===void 0){const n=Df.get(e);if(n!==void 0)t=Le[n],console.warn('THREE.WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',e,n);else throw new Error("Can not resolve #include <"+e+">")}return Ws(t)}const If=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function io(i){return i.replace(If,Nf)}function Nf(i,e,t,n){let r="";for(let s=parseInt(e);s<parseInt(t);s++)r+=n.replace(/\[\s*i\s*\]/g,"[ "+s+" ]").replace(/UNROLLED_LOOP_INDEX/g,s);return r}function ro(i){let e=`precision ${i.precision} float;
	precision ${i.precision} int;
	precision ${i.precision} sampler2D;
	precision ${i.precision} samplerCube;
	precision ${i.precision} sampler3D;
	precision ${i.precision} sampler2DArray;
	precision ${i.precision} sampler2DShadow;
	precision ${i.precision} samplerCubeShadow;
	precision ${i.precision} sampler2DArrayShadow;
	precision ${i.precision} isampler2D;
	precision ${i.precision} isampler3D;
	precision ${i.precision} isamplerCube;
	precision ${i.precision} isampler2DArray;
	precision ${i.precision} usampler2D;
	precision ${i.precision} usampler3D;
	precision ${i.precision} usamplerCube;
	precision ${i.precision} usampler2DArray;
	`;return i.precision==="highp"?e+=`
#define HIGH_PRECISION`:i.precision==="mediump"?e+=`
#define MEDIUM_PRECISION`:i.precision==="lowp"&&(e+=`
#define LOW_PRECISION`),e}function Ff(i){let e="SHADOWMAP_TYPE_BASIC";return i.shadowMapType===vo?e="SHADOWMAP_TYPE_PCF":i.shadowMapType===Ml?e="SHADOWMAP_TYPE_PCF_SOFT":i.shadowMapType===Jt&&(e="SHADOWMAP_TYPE_VSM"),e}function Of(i){let e="ENVMAP_TYPE_CUBE";if(i.envMap)switch(i.envMapMode){case Jn:case Qn:e="ENVMAP_TYPE_CUBE";break;case fr:e="ENVMAP_TYPE_CUBE_UV";break}return e}function Bf(i){let e="ENVMAP_MODE_REFLECTION";if(i.envMap)switch(i.envMapMode){case Qn:e="ENVMAP_MODE_REFRACTION";break}return e}function zf(i){let e="ENVMAP_BLENDING_NONE";if(i.envMap)switch(i.combine){case xo:e="ENVMAP_BLENDING_MULTIPLY";break;case zl:e="ENVMAP_BLENDING_MIX";break;case Hl:e="ENVMAP_BLENDING_ADD";break}return e}function Hf(i){const e=i.envMapCubeUVHeight;if(e===null)return null;const t=Math.log2(e)-2,n=1/e;return{texelWidth:1/(3*Math.max(Math.pow(2,t),7*16)),texelHeight:n,maxMip:t}}function Gf(i,e,t,n){const r=i.getContext(),s=t.defines;let a=t.vertexShader,o=t.fragmentShader;const l=Ff(t),c=Of(t),d=Bf(t),p=zf(t),f=Hf(t),m=Rf(t),_=Cf(s),M=r.createProgram();let h,u,y=t.glslVersion?"#version "+t.glslVersion+`
`:"";t.isRawShaderMaterial?(h=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(_i).join(`
`),h.length>0&&(h+=`
`),u=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(_i).join(`
`),u.length>0&&(u+=`
`)):(h=[ro(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",t.batching?"#define USE_BATCHING":"",t.batchingColor?"#define USE_BATCHING_COLOR":"",t.instancing?"#define USE_INSTANCING":"",t.instancingColor?"#define USE_INSTANCING_COLOR":"",t.instancingMorph?"#define USE_INSTANCING_MORPH":"",t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.map?"#define USE_MAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+d:"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.displacementMap?"#define USE_DISPLACEMENTMAP":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.mapUv?"#define MAP_UV "+t.mapUv:"",t.alphaMapUv?"#define ALPHAMAP_UV "+t.alphaMapUv:"",t.lightMapUv?"#define LIGHTMAP_UV "+t.lightMapUv:"",t.aoMapUv?"#define AOMAP_UV "+t.aoMapUv:"",t.emissiveMapUv?"#define EMISSIVEMAP_UV "+t.emissiveMapUv:"",t.bumpMapUv?"#define BUMPMAP_UV "+t.bumpMapUv:"",t.normalMapUv?"#define NORMALMAP_UV "+t.normalMapUv:"",t.displacementMapUv?"#define DISPLACEMENTMAP_UV "+t.displacementMapUv:"",t.metalnessMapUv?"#define METALNESSMAP_UV "+t.metalnessMapUv:"",t.roughnessMapUv?"#define ROUGHNESSMAP_UV "+t.roughnessMapUv:"",t.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+t.anisotropyMapUv:"",t.clearcoatMapUv?"#define CLEARCOATMAP_UV "+t.clearcoatMapUv:"",t.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+t.clearcoatNormalMapUv:"",t.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+t.clearcoatRoughnessMapUv:"",t.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+t.iridescenceMapUv:"",t.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+t.iridescenceThicknessMapUv:"",t.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+t.sheenColorMapUv:"",t.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+t.sheenRoughnessMapUv:"",t.specularMapUv?"#define SPECULARMAP_UV "+t.specularMapUv:"",t.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+t.specularColorMapUv:"",t.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+t.specularIntensityMapUv:"",t.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+t.transmissionMapUv:"",t.thicknessMapUv?"#define THICKNESSMAP_UV "+t.thicknessMapUv:"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors?"#define USE_COLOR":"",t.vertexAlphas?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.flatShading?"#define FLAT_SHADED":"",t.skinning?"#define USE_SKINNING":"",t.morphTargets?"#define USE_MORPHTARGETS":"",t.morphNormals&&t.flatShading===!1?"#define USE_MORPHNORMALS":"",t.morphColors?"#define USE_MORPHCOLORS":"",t.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+t.morphTextureStride:"",t.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+t.morphTargetsCount:"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+l:"",t.sizeAttenuation?"#define USE_SIZEATTENUATION":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.logarithmicDepthBuffer?"#define USE_LOGDEPTHBUF":"",t.reverseDepthBuffer?"#define USE_REVERSEDEPTHBUF":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(_i).join(`
`),u=[ro(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",t.map?"#define USE_MAP":"",t.matcap?"#define USE_MATCAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+c:"",t.envMap?"#define "+d:"",t.envMap?"#define "+p:"",f?"#define CUBEUV_TEXEL_WIDTH "+f.texelWidth:"",f?"#define CUBEUV_TEXEL_HEIGHT "+f.texelHeight:"",f?"#define CUBEUV_MAX_MIP "+f.maxMip+".0":"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoat?"#define USE_CLEARCOAT":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.dispersion?"#define USE_DISPERSION":"",t.iridescence?"#define USE_IRIDESCENCE":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaTest?"#define USE_ALPHATEST":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.sheen?"#define USE_SHEEN":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors||t.instancingColor||t.batchingColor?"#define USE_COLOR":"",t.vertexAlphas?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.gradientMap?"#define USE_GRADIENTMAP":"",t.flatShading?"#define FLAT_SHADED":"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+l:"",t.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",t.logarithmicDepthBuffer?"#define USE_LOGDEPTHBUF":"",t.reverseDepthBuffer?"#define USE_REVERSEDEPTHBUF":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",t.toneMapping!==fn?"#define TONE_MAPPING":"",t.toneMapping!==fn?Le.tonemapping_pars_fragment:"",t.toneMapping!==fn?bf("toneMapping",t.toneMapping):"",t.dithering?"#define DITHERING":"",t.opaque?"#define OPAQUE":"",Le.colorspace_pars_fragment,Af("linearToOutputTexel",t.outputColorSpace),wf(),t.useDepthPacking?"#define DEPTH_PACKING "+t.depthPacking:"",`
`].filter(_i).join(`
`)),a=Ws(a),a=to(a,t),a=no(a,t),o=Ws(o),o=to(o,t),o=no(o,t),a=io(a),o=io(o),t.isRawShaderMaterial!==!0&&(y=`#version 300 es
`,h=[m,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+h,u=["#define varying in",t.glslVersion===Sa?"":"layout(location = 0) out highp vec4 pc_fragColor;",t.glslVersion===Sa?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+u);const S=y+h+a,T=y+u+o,N=Qa(r,r.VERTEX_SHADER,S),R=Qa(r,r.FRAGMENT_SHADER,T);r.attachShader(M,N),r.attachShader(M,R),t.index0AttributeName!==void 0?r.bindAttribLocation(M,0,t.index0AttributeName):t.morphTargets===!0&&r.bindAttribLocation(M,0,"position"),r.linkProgram(M);function w(E){if(i.debug.checkShaderErrors){const k=r.getProgramInfoLog(M).trim(),G=r.getShaderInfoLog(N).trim(),X=r.getShaderInfoLog(R).trim();let J=!0,H=!0;if(r.getProgramParameter(M,r.LINK_STATUS)===!1)if(J=!1,typeof i.debug.onShaderError=="function")i.debug.onShaderError(r,M,N,R);else{const Q=eo(r,N,"vertex"),W=eo(r,R,"fragment");console.error("THREE.WebGLProgram: Shader Error "+r.getError()+" - VALIDATE_STATUS "+r.getProgramParameter(M,r.VALIDATE_STATUS)+`

Material Name: `+E.name+`
Material Type: `+E.type+`

Program Info Log: `+k+`
`+Q+`
`+W)}else k!==""?console.warn("THREE.WebGLProgram: Program Info Log:",k):(G===""||X==="")&&(H=!1);H&&(E.diagnostics={runnable:J,programLog:k,vertexShader:{log:G,prefix:h},fragmentShader:{log:X,prefix:u}})}r.deleteShader(N),r.deleteShader(R),I=new ir(r,M),K=Pf(r,M)}let I;this.getUniforms=function(){return I===void 0&&w(this),I};let K;this.getAttributes=function(){return K===void 0&&w(this),K};let g=t.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return g===!1&&(g=r.getProgramParameter(M,Sf)),g},this.destroy=function(){n.releaseStatesOfProgram(this),r.deleteProgram(M),this.program=void 0},this.type=t.shaderType,this.name=t.shaderName,this.id=Ef++,this.cacheKey=e,this.usedTimes=1,this.program=M,this.vertexShader=N,this.fragmentShader=R,this}let Vf=0;class kf{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(e){const t=e.vertexShader,n=e.fragmentShader,r=this._getShaderStage(t),s=this._getShaderStage(n),a=this._getShaderCacheForMaterial(e);return a.has(r)===!1&&(a.add(r),r.usedTimes++),a.has(s)===!1&&(a.add(s),s.usedTimes++),this}remove(e){const t=this.materialCache.get(e);for(const n of t)n.usedTimes--,n.usedTimes===0&&this.shaderCache.delete(n.code);return this.materialCache.delete(e),this}getVertexShaderID(e){return this._getShaderStage(e.vertexShader).id}getFragmentShaderID(e){return this._getShaderStage(e.fragmentShader).id}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(e){const t=this.materialCache;let n=t.get(e);return n===void 0&&(n=new Set,t.set(e,n)),n}_getShaderStage(e){const t=this.shaderCache;let n=t.get(e);return n===void 0&&(n=new Wf(e),t.set(e,n)),n}}class Wf{constructor(e){this.id=Vf++,this.code=e,this.usedTimes=0}}function Xf(i,e,t,n,r,s,a){const o=new Oo,l=new kf,c=new Set,d=[],p=r.logarithmicDepthBuffer,f=r.reverseDepthBuffer,m=r.vertexTextures;let _=r.precision;const M={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distanceRGBA",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function h(g){return c.add(g),g===0?"uv":`uv${g}`}function u(g,E,k,G,X){const J=G.fog,H=X.geometry,Q=g.isMeshStandardMaterial?G.environment:null,W=(g.isMeshStandardMaterial?t:e).get(g.envMap||Q),le=W&&W.mapping===fr?W.image.height:null,ce=M[g.type];g.precision!==null&&(_=r.getMaxPrecision(g.precision),_!==g.precision&&console.warn("THREE.WebGLProgram.getParameters:",g.precision,"not supported, using",_,"instead."));const _e=H.morphAttributes.position||H.morphAttributes.normal||H.morphAttributes.color,He=_e!==void 0?_e.length:0;let Ve=0;H.morphAttributes.position!==void 0&&(Ve=1),H.morphAttributes.normal!==void 0&&(Ve=2),H.morphAttributes.color!==void 0&&(Ve=3);let b,B,ie,Z;if(ce){const _t=kt[ce];b=_t.vertexShader,B=_t.fragmentShader}else b=g.vertexShader,B=g.fragmentShader,l.update(g),ie=l.getVertexShaderID(g),Z=l.getFragmentShaderID(g);const ge=i.getRenderTarget(),ee=X.isInstancedMesh===!0,be=X.isBatchedMesh===!0,Ne=!!g.map,Se=!!g.matcap,C=!!W,it=!!g.aoMap,Ue=!!g.lightMap,Ie=!!g.bumpMap,Te=!!g.normalMap,ke=!!g.displacementMap,Ee=!!g.emissiveMap,A=!!g.metalnessMap,v=!!g.roughnessMap,F=g.anisotropy>0,Y=g.clearcoat>0,j=g.dispersion>0,q=g.iridescence>0,xe=g.sheen>0,se=g.transmission>0,de=F&&!!g.anisotropyMap,We=Y&&!!g.clearcoatMap,te=Y&&!!g.clearcoatNormalMap,fe=Y&&!!g.clearcoatRoughnessMap,Re=q&&!!g.iridescenceMap,Ce=q&&!!g.iridescenceThicknessMap,pe=xe&&!!g.sheenColorMap,Be=xe&&!!g.sheenRoughnessMap,Pe=!!g.specularMap,je=!!g.specularColorMap,P=!!g.specularIntensityMap,ue=se&&!!g.transmissionMap,V=se&&!!g.thicknessMap,$=!!g.gradientMap,ae=!!g.alphaMap,he=g.alphaTest>0,Ge=!!g.alphaHash,rt=!!g.extensions;let mt=fn;g.toneMapped&&(ge===null||ge.isXRRenderTarget===!0)&&(mt=i.toneMapping);const Xe={shaderID:ce,shaderType:g.type,shaderName:g.name,vertexShader:b,fragmentShader:B,defines:g.defines,customVertexShaderID:ie,customFragmentShaderID:Z,isRawShaderMaterial:g.isRawShaderMaterial===!0,glslVersion:g.glslVersion,precision:_,batching:be,batchingColor:be&&X._colorsTexture!==null,instancing:ee,instancingColor:ee&&X.instanceColor!==null,instancingMorph:ee&&X.morphTexture!==null,supportsVertexTextures:m,outputColorSpace:ge===null?i.outputColorSpace:ge.isXRRenderTarget===!0?ge.texture.colorSpace:_n,alphaToCoverage:!!g.alphaToCoverage,map:Ne,matcap:Se,envMap:C,envMapMode:C&&W.mapping,envMapCubeUVHeight:le,aoMap:it,lightMap:Ue,bumpMap:Ie,normalMap:Te,displacementMap:m&&ke,emissiveMap:Ee,normalMapObjectSpace:Te&&g.normalMapType===jl,normalMapTangentSpace:Te&&g.normalMapType===Lo,metalnessMap:A,roughnessMap:v,anisotropy:F,anisotropyMap:de,clearcoat:Y,clearcoatMap:We,clearcoatNormalMap:te,clearcoatRoughnessMap:fe,dispersion:j,iridescence:q,iridescenceMap:Re,iridescenceThicknessMap:Ce,sheen:xe,sheenColorMap:pe,sheenRoughnessMap:Be,specularMap:Pe,specularColorMap:je,specularIntensityMap:P,transmission:se,transmissionMap:ue,thicknessMap:V,gradientMap:$,opaque:g.transparent===!1&&g.blending===Kn&&g.alphaToCoverage===!1,alphaMap:ae,alphaTest:he,alphaHash:Ge,combine:g.combine,mapUv:Ne&&h(g.map.channel),aoMapUv:it&&h(g.aoMap.channel),lightMapUv:Ue&&h(g.lightMap.channel),bumpMapUv:Ie&&h(g.bumpMap.channel),normalMapUv:Te&&h(g.normalMap.channel),displacementMapUv:ke&&h(g.displacementMap.channel),emissiveMapUv:Ee&&h(g.emissiveMap.channel),metalnessMapUv:A&&h(g.metalnessMap.channel),roughnessMapUv:v&&h(g.roughnessMap.channel),anisotropyMapUv:de&&h(g.anisotropyMap.channel),clearcoatMapUv:We&&h(g.clearcoatMap.channel),clearcoatNormalMapUv:te&&h(g.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:fe&&h(g.clearcoatRoughnessMap.channel),iridescenceMapUv:Re&&h(g.iridescenceMap.channel),iridescenceThicknessMapUv:Ce&&h(g.iridescenceThicknessMap.channel),sheenColorMapUv:pe&&h(g.sheenColorMap.channel),sheenRoughnessMapUv:Be&&h(g.sheenRoughnessMap.channel),specularMapUv:Pe&&h(g.specularMap.channel),specularColorMapUv:je&&h(g.specularColorMap.channel),specularIntensityMapUv:P&&h(g.specularIntensityMap.channel),transmissionMapUv:ue&&h(g.transmissionMap.channel),thicknessMapUv:V&&h(g.thicknessMap.channel),alphaMapUv:ae&&h(g.alphaMap.channel),vertexTangents:!!H.attributes.tangent&&(Te||F),vertexColors:g.vertexColors,vertexAlphas:g.vertexColors===!0&&!!H.attributes.color&&H.attributes.color.itemSize===4,pointsUvs:X.isPoints===!0&&!!H.attributes.uv&&(Ne||ae),fog:!!J,useFog:g.fog===!0,fogExp2:!!J&&J.isFogExp2,flatShading:g.flatShading===!0,sizeAttenuation:g.sizeAttenuation===!0,logarithmicDepthBuffer:p,reverseDepthBuffer:f,skinning:X.isSkinnedMesh===!0,morphTargets:H.morphAttributes.position!==void 0,morphNormals:H.morphAttributes.normal!==void 0,morphColors:H.morphAttributes.color!==void 0,morphTargetsCount:He,morphTextureStride:Ve,numDirLights:E.directional.length,numPointLights:E.point.length,numSpotLights:E.spot.length,numSpotLightMaps:E.spotLightMap.length,numRectAreaLights:E.rectArea.length,numHemiLights:E.hemi.length,numDirLightShadows:E.directionalShadowMap.length,numPointLightShadows:E.pointShadowMap.length,numSpotLightShadows:E.spotShadowMap.length,numSpotLightShadowsWithMaps:E.numSpotLightShadowsWithMaps,numLightProbes:E.numLightProbes,numClippingPlanes:a.numPlanes,numClipIntersection:a.numIntersection,dithering:g.dithering,shadowMapEnabled:i.shadowMap.enabled&&k.length>0,shadowMapType:i.shadowMap.type,toneMapping:mt,decodeVideoTexture:Ne&&g.map.isVideoTexture===!0&&Ke.getTransfer(g.map.colorSpace)===Qe,premultipliedAlpha:g.premultipliedAlpha,doubleSided:g.side===Qt,flipSided:g.side===Mt,useDepthPacking:g.depthPacking>=0,depthPacking:g.depthPacking||0,index0AttributeName:g.index0AttributeName,extensionClipCullDistance:rt&&g.extensions.clipCullDistance===!0&&n.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(rt&&g.extensions.multiDraw===!0||be)&&n.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:n.has("KHR_parallel_shader_compile"),customProgramCacheKey:g.customProgramCacheKey()};return Xe.vertexUv1s=c.has(1),Xe.vertexUv2s=c.has(2),Xe.vertexUv3s=c.has(3),c.clear(),Xe}function y(g){const E=[];if(g.shaderID?E.push(g.shaderID):(E.push(g.customVertexShaderID),E.push(g.customFragmentShaderID)),g.defines!==void 0)for(const k in g.defines)E.push(k),E.push(g.defines[k]);return g.isRawShaderMaterial===!1&&(S(E,g),T(E,g),E.push(i.outputColorSpace)),E.push(g.customProgramCacheKey),E.join()}function S(g,E){g.push(E.precision),g.push(E.outputColorSpace),g.push(E.envMapMode),g.push(E.envMapCubeUVHeight),g.push(E.mapUv),g.push(E.alphaMapUv),g.push(E.lightMapUv),g.push(E.aoMapUv),g.push(E.bumpMapUv),g.push(E.normalMapUv),g.push(E.displacementMapUv),g.push(E.emissiveMapUv),g.push(E.metalnessMapUv),g.push(E.roughnessMapUv),g.push(E.anisotropyMapUv),g.push(E.clearcoatMapUv),g.push(E.clearcoatNormalMapUv),g.push(E.clearcoatRoughnessMapUv),g.push(E.iridescenceMapUv),g.push(E.iridescenceThicknessMapUv),g.push(E.sheenColorMapUv),g.push(E.sheenRoughnessMapUv),g.push(E.specularMapUv),g.push(E.specularColorMapUv),g.push(E.specularIntensityMapUv),g.push(E.transmissionMapUv),g.push(E.thicknessMapUv),g.push(E.combine),g.push(E.fogExp2),g.push(E.sizeAttenuation),g.push(E.morphTargetsCount),g.push(E.morphAttributeCount),g.push(E.numDirLights),g.push(E.numPointLights),g.push(E.numSpotLights),g.push(E.numSpotLightMaps),g.push(E.numHemiLights),g.push(E.numRectAreaLights),g.push(E.numDirLightShadows),g.push(E.numPointLightShadows),g.push(E.numSpotLightShadows),g.push(E.numSpotLightShadowsWithMaps),g.push(E.numLightProbes),g.push(E.shadowMapType),g.push(E.toneMapping),g.push(E.numClippingPlanes),g.push(E.numClipIntersection),g.push(E.depthPacking)}function T(g,E){o.disableAll(),E.supportsVertexTextures&&o.enable(0),E.instancing&&o.enable(1),E.instancingColor&&o.enable(2),E.instancingMorph&&o.enable(3),E.matcap&&o.enable(4),E.envMap&&o.enable(5),E.normalMapObjectSpace&&o.enable(6),E.normalMapTangentSpace&&o.enable(7),E.clearcoat&&o.enable(8),E.iridescence&&o.enable(9),E.alphaTest&&o.enable(10),E.vertexColors&&o.enable(11),E.vertexAlphas&&o.enable(12),E.vertexUv1s&&o.enable(13),E.vertexUv2s&&o.enable(14),E.vertexUv3s&&o.enable(15),E.vertexTangents&&o.enable(16),E.anisotropy&&o.enable(17),E.alphaHash&&o.enable(18),E.batching&&o.enable(19),E.dispersion&&o.enable(20),E.batchingColor&&o.enable(21),g.push(o.mask),o.disableAll(),E.fog&&o.enable(0),E.useFog&&o.enable(1),E.flatShading&&o.enable(2),E.logarithmicDepthBuffer&&o.enable(3),E.reverseDepthBuffer&&o.enable(4),E.skinning&&o.enable(5),E.morphTargets&&o.enable(6),E.morphNormals&&o.enable(7),E.morphColors&&o.enable(8),E.premultipliedAlpha&&o.enable(9),E.shadowMapEnabled&&o.enable(10),E.doubleSided&&o.enable(11),E.flipSided&&o.enable(12),E.useDepthPacking&&o.enable(13),E.dithering&&o.enable(14),E.transmission&&o.enable(15),E.sheen&&o.enable(16),E.opaque&&o.enable(17),E.pointsUvs&&o.enable(18),E.decodeVideoTexture&&o.enable(19),E.alphaToCoverage&&o.enable(20),g.push(o.mask)}function N(g){const E=M[g.type];let k;if(E){const G=kt[E];k=wc.clone(G.uniforms)}else k=g.uniforms;return k}function R(g,E){let k;for(let G=0,X=d.length;G<X;G++){const J=d[G];if(J.cacheKey===E){k=J,++k.usedTimes;break}}return k===void 0&&(k=new Gf(i,E,g,s),d.push(k)),k}function w(g){if(--g.usedTimes===0){const E=d.indexOf(g);d[E]=d[d.length-1],d.pop(),g.destroy()}}function I(g){l.remove(g)}function K(){l.dispose()}return{getParameters:u,getProgramCacheKey:y,getUniforms:N,acquireProgram:R,releaseProgram:w,releaseShaderCache:I,programs:d,dispose:K}}function qf(){let i=new WeakMap;function e(a){return i.has(a)}function t(a){let o=i.get(a);return o===void 0&&(o={},i.set(a,o)),o}function n(a){i.delete(a)}function r(a,o,l){i.get(a)[o]=l}function s(){i=new WeakMap}return{has:e,get:t,remove:n,update:r,dispose:s}}function Yf(i,e){return i.groupOrder!==e.groupOrder?i.groupOrder-e.groupOrder:i.renderOrder!==e.renderOrder?i.renderOrder-e.renderOrder:i.material.id!==e.material.id?i.material.id-e.material.id:i.z!==e.z?i.z-e.z:i.id-e.id}function so(i,e){return i.groupOrder!==e.groupOrder?i.groupOrder-e.groupOrder:i.renderOrder!==e.renderOrder?i.renderOrder-e.renderOrder:i.z!==e.z?e.z-i.z:i.id-e.id}function ao(){const i=[];let e=0;const t=[],n=[],r=[];function s(){e=0,t.length=0,n.length=0,r.length=0}function a(p,f,m,_,M,h){let u=i[e];return u===void 0?(u={id:p.id,object:p,geometry:f,material:m,groupOrder:_,renderOrder:p.renderOrder,z:M,group:h},i[e]=u):(u.id=p.id,u.object=p,u.geometry=f,u.material=m,u.groupOrder=_,u.renderOrder=p.renderOrder,u.z=M,u.group=h),e++,u}function o(p,f,m,_,M,h){const u=a(p,f,m,_,M,h);m.transmission>0?n.push(u):m.transparent===!0?r.push(u):t.push(u)}function l(p,f,m,_,M,h){const u=a(p,f,m,_,M,h);m.transmission>0?n.unshift(u):m.transparent===!0?r.unshift(u):t.unshift(u)}function c(p,f){t.length>1&&t.sort(p||Yf),n.length>1&&n.sort(f||so),r.length>1&&r.sort(f||so)}function d(){for(let p=e,f=i.length;p<f;p++){const m=i[p];if(m.id===null)break;m.id=null,m.object=null,m.geometry=null,m.material=null,m.group=null}}return{opaque:t,transmissive:n,transparent:r,init:s,push:o,unshift:l,finish:d,sort:c}}function Kf(){let i=new WeakMap;function e(n,r){const s=i.get(n);let a;return s===void 0?(a=new ao,i.set(n,[a])):r>=s.length?(a=new ao,s.push(a)):a=s[r],a}function t(){i=new WeakMap}return{get:e,dispose:t}}function $f(){const i={};return{get:function(e){if(i[e.id]!==void 0)return i[e.id];let t;switch(e.type){case"DirectionalLight":t={direction:new D,color:new Oe};break;case"SpotLight":t={position:new D,direction:new D,color:new Oe,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":t={position:new D,color:new Oe,distance:0,decay:0};break;case"HemisphereLight":t={direction:new D,skyColor:new Oe,groundColor:new Oe};break;case"RectAreaLight":t={color:new Oe,position:new D,halfWidth:new D,halfHeight:new D};break}return i[e.id]=t,t}}}function Zf(){const i={};return{get:function(e){if(i[e.id]!==void 0)return i[e.id];let t;switch(e.type){case"DirectionalLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new ze};break;case"SpotLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new ze};break;case"PointLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new ze,shadowCameraNear:1,shadowCameraFar:1e3};break}return i[e.id]=t,t}}}let jf=0;function Jf(i,e){return(e.castShadow?2:0)-(i.castShadow?2:0)+(e.map?1:0)-(i.map?1:0)}function Qf(i){const e=new $f,t=Zf(),n={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let c=0;c<9;c++)n.probe.push(new D);const r=new D,s=new et,a=new et;function o(c){let d=0,p=0,f=0;for(let K=0;K<9;K++)n.probe[K].set(0,0,0);let m=0,_=0,M=0,h=0,u=0,y=0,S=0,T=0,N=0,R=0,w=0;c.sort(Jf);for(let K=0,g=c.length;K<g;K++){const E=c[K],k=E.color,G=E.intensity,X=E.distance,J=E.shadow&&E.shadow.map?E.shadow.map.texture:null;if(E.isAmbientLight)d+=k.r*G,p+=k.g*G,f+=k.b*G;else if(E.isLightProbe){for(let H=0;H<9;H++)n.probe[H].addScaledVector(E.sh.coefficients[H],G);w++}else if(E.isDirectionalLight){const H=e.get(E);if(H.color.copy(E.color).multiplyScalar(E.intensity),E.castShadow){const Q=E.shadow,W=t.get(E);W.shadowIntensity=Q.intensity,W.shadowBias=Q.bias,W.shadowNormalBias=Q.normalBias,W.shadowRadius=Q.radius,W.shadowMapSize=Q.mapSize,n.directionalShadow[m]=W,n.directionalShadowMap[m]=J,n.directionalShadowMatrix[m]=E.shadow.matrix,y++}n.directional[m]=H,m++}else if(E.isSpotLight){const H=e.get(E);H.position.setFromMatrixPosition(E.matrixWorld),H.color.copy(k).multiplyScalar(G),H.distance=X,H.coneCos=Math.cos(E.angle),H.penumbraCos=Math.cos(E.angle*(1-E.penumbra)),H.decay=E.decay,n.spot[M]=H;const Q=E.shadow;if(E.map&&(n.spotLightMap[N]=E.map,N++,Q.updateMatrices(E),E.castShadow&&R++),n.spotLightMatrix[M]=Q.matrix,E.castShadow){const W=t.get(E);W.shadowIntensity=Q.intensity,W.shadowBias=Q.bias,W.shadowNormalBias=Q.normalBias,W.shadowRadius=Q.radius,W.shadowMapSize=Q.mapSize,n.spotShadow[M]=W,n.spotShadowMap[M]=J,T++}M++}else if(E.isRectAreaLight){const H=e.get(E);H.color.copy(k).multiplyScalar(G),H.halfWidth.set(E.width*.5,0,0),H.halfHeight.set(0,E.height*.5,0),n.rectArea[h]=H,h++}else if(E.isPointLight){const H=e.get(E);if(H.color.copy(E.color).multiplyScalar(E.intensity),H.distance=E.distance,H.decay=E.decay,E.castShadow){const Q=E.shadow,W=t.get(E);W.shadowIntensity=Q.intensity,W.shadowBias=Q.bias,W.shadowNormalBias=Q.normalBias,W.shadowRadius=Q.radius,W.shadowMapSize=Q.mapSize,W.shadowCameraNear=Q.camera.near,W.shadowCameraFar=Q.camera.far,n.pointShadow[_]=W,n.pointShadowMap[_]=J,n.pointShadowMatrix[_]=E.shadow.matrix,S++}n.point[_]=H,_++}else if(E.isHemisphereLight){const H=e.get(E);H.skyColor.copy(E.color).multiplyScalar(G),H.groundColor.copy(E.groundColor).multiplyScalar(G),n.hemi[u]=H,u++}}h>0&&(i.has("OES_texture_float_linear")===!0?(n.rectAreaLTC1=re.LTC_FLOAT_1,n.rectAreaLTC2=re.LTC_FLOAT_2):(n.rectAreaLTC1=re.LTC_HALF_1,n.rectAreaLTC2=re.LTC_HALF_2)),n.ambient[0]=d,n.ambient[1]=p,n.ambient[2]=f;const I=n.hash;(I.directionalLength!==m||I.pointLength!==_||I.spotLength!==M||I.rectAreaLength!==h||I.hemiLength!==u||I.numDirectionalShadows!==y||I.numPointShadows!==S||I.numSpotShadows!==T||I.numSpotMaps!==N||I.numLightProbes!==w)&&(n.directional.length=m,n.spot.length=M,n.rectArea.length=h,n.point.length=_,n.hemi.length=u,n.directionalShadow.length=y,n.directionalShadowMap.length=y,n.pointShadow.length=S,n.pointShadowMap.length=S,n.spotShadow.length=T,n.spotShadowMap.length=T,n.directionalShadowMatrix.length=y,n.pointShadowMatrix.length=S,n.spotLightMatrix.length=T+N-R,n.spotLightMap.length=N,n.numSpotLightShadowsWithMaps=R,n.numLightProbes=w,I.directionalLength=m,I.pointLength=_,I.spotLength=M,I.rectAreaLength=h,I.hemiLength=u,I.numDirectionalShadows=y,I.numPointShadows=S,I.numSpotShadows=T,I.numSpotMaps=N,I.numLightProbes=w,n.version=jf++)}function l(c,d){let p=0,f=0,m=0,_=0,M=0;const h=d.matrixWorldInverse;for(let u=0,y=c.length;u<y;u++){const S=c[u];if(S.isDirectionalLight){const T=n.directional[p];T.direction.setFromMatrixPosition(S.matrixWorld),r.setFromMatrixPosition(S.target.matrixWorld),T.direction.sub(r),T.direction.transformDirection(h),p++}else if(S.isSpotLight){const T=n.spot[m];T.position.setFromMatrixPosition(S.matrixWorld),T.position.applyMatrix4(h),T.direction.setFromMatrixPosition(S.matrixWorld),r.setFromMatrixPosition(S.target.matrixWorld),T.direction.sub(r),T.direction.transformDirection(h),m++}else if(S.isRectAreaLight){const T=n.rectArea[_];T.position.setFromMatrixPosition(S.matrixWorld),T.position.applyMatrix4(h),a.identity(),s.copy(S.matrixWorld),s.premultiply(h),a.extractRotation(s),T.halfWidth.set(S.width*.5,0,0),T.halfHeight.set(0,S.height*.5,0),T.halfWidth.applyMatrix4(a),T.halfHeight.applyMatrix4(a),_++}else if(S.isPointLight){const T=n.point[f];T.position.setFromMatrixPosition(S.matrixWorld),T.position.applyMatrix4(h),f++}else if(S.isHemisphereLight){const T=n.hemi[M];T.direction.setFromMatrixPosition(S.matrixWorld),T.direction.transformDirection(h),M++}}}return{setup:o,setupView:l,state:n}}function oo(i){const e=new Qf(i),t=[],n=[];function r(d){c.camera=d,t.length=0,n.length=0}function s(d){t.push(d)}function a(d){n.push(d)}function o(){e.setup(t)}function l(d){e.setupView(t,d)}const c={lightsArray:t,shadowsArray:n,camera:null,lights:e,transmissionRenderTarget:{}};return{init:r,state:c,setupLights:o,setupLightsView:l,pushLight:s,pushShadow:a}}function ep(i){let e=new WeakMap;function t(r,s=0){const a=e.get(r);let o;return a===void 0?(o=new oo(i),e.set(r,[o])):s>=a.length?(o=new oo(i),a.push(o)):o=a[s],o}function n(){e=new WeakMap}return{get:t,dispose:n}}class tp extends ri{constructor(e){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=$l,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(e)}copy(e){return super.copy(e),this.depthPacking=e.depthPacking,this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this}}class np extends ri{constructor(e){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(e)}copy(e){return super.copy(e),this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this}}const ip=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,rp=`uniform sampler2D shadow_pass;
uniform vec2 resolution;
uniform float radius;
#include <packing>
void main() {
	const float samples = float( VSM_SAMPLES );
	float mean = 0.0;
	float squared_mean = 0.0;
	float uvStride = samples <= 1.0 ? 0.0 : 2.0 / ( samples - 1.0 );
	float uvStart = samples <= 1.0 ? 0.0 : - 1.0;
	for ( float i = 0.0; i < samples; i ++ ) {
		float uvOffset = uvStart + i * uvStride;
		#ifdef HORIZONTAL_PASS
			vec2 distribution = unpackRGBATo2Half( texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( uvOffset, 0.0 ) * radius ) / resolution ) );
			mean += distribution.x;
			squared_mean += distribution.y * distribution.y + distribution.x * distribution.x;
		#else
			float depth = unpackRGBAToDepth( texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( 0.0, uvOffset ) * radius ) / resolution ) );
			mean += depth;
			squared_mean += depth * depth;
		#endif
	}
	mean = mean / samples;
	squared_mean = squared_mean / samples;
	float std_dev = sqrt( squared_mean - mean * mean );
	gl_FragColor = pack2HalfToRGBA( vec2( mean, std_dev ) );
}`;function sp(i,e,t){let n=new Qs;const r=new ze,s=new ze,a=new Ze,o=new tp({depthPacking:Zl}),l=new np,c={},d=t.maxTextureSize,p={[pn]:Mt,[Mt]:pn,[Qt]:Qt},f=new mn({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new ze},radius:{value:4}},vertexShader:ip,fragmentShader:rp}),m=f.clone();m.defines.HORIZONTAL_PASS=1;const _=new Rt;_.setAttribute("position",new Wt(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const M=new wt(_,f),h=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=vo;let u=this.type;this.render=function(R,w,I){if(h.enabled===!1||h.autoUpdate===!1&&h.needsUpdate===!1||R.length===0)return;const K=i.getRenderTarget(),g=i.getActiveCubeFace(),E=i.getActiveMipmapLevel(),k=i.state;k.setBlending(dn),k.buffers.color.setClear(1,1,1,1),k.buffers.depth.setTest(!0),k.setScissorTest(!1);const G=u!==Jt&&this.type===Jt,X=u===Jt&&this.type!==Jt;for(let J=0,H=R.length;J<H;J++){const Q=R[J],W=Q.shadow;if(W===void 0){console.warn("THREE.WebGLShadowMap:",Q,"has no shadow.");continue}if(W.autoUpdate===!1&&W.needsUpdate===!1)continue;r.copy(W.mapSize);const le=W.getFrameExtents();if(r.multiply(le),s.copy(W.mapSize),(r.x>d||r.y>d)&&(r.x>d&&(s.x=Math.floor(d/le.x),r.x=s.x*le.x,W.mapSize.x=s.x),r.y>d&&(s.y=Math.floor(d/le.y),r.y=s.y*le.y,W.mapSize.y=s.y)),W.map===null||G===!0||X===!0){const _e=this.type!==Jt?{minFilter:Lt,magFilter:Lt}:{};W.map!==null&&W.map.dispose(),W.map=new Ln(r.x,r.y,_e),W.map.texture.name=Q.name+".shadowMap",W.camera.updateProjectionMatrix()}i.setRenderTarget(W.map),i.clear();const ce=W.getViewportCount();for(let _e=0;_e<ce;_e++){const He=W.getViewport(_e);a.set(s.x*He.x,s.y*He.y,s.x*He.z,s.y*He.w),k.viewport(a),W.updateMatrices(Q,_e),n=W.getFrustum(),T(w,I,W.camera,Q,this.type)}W.isPointLightShadow!==!0&&this.type===Jt&&y(W,I),W.needsUpdate=!1}u=this.type,h.needsUpdate=!1,i.setRenderTarget(K,g,E)};function y(R,w){const I=e.update(M);f.defines.VSM_SAMPLES!==R.blurSamples&&(f.defines.VSM_SAMPLES=R.blurSamples,m.defines.VSM_SAMPLES=R.blurSamples,f.needsUpdate=!0,m.needsUpdate=!0),R.mapPass===null&&(R.mapPass=new Ln(r.x,r.y)),f.uniforms.shadow_pass.value=R.map.texture,f.uniforms.resolution.value=R.mapSize,f.uniforms.radius.value=R.radius,i.setRenderTarget(R.mapPass),i.clear(),i.renderBufferDirect(w,null,I,f,M,null),m.uniforms.shadow_pass.value=R.mapPass.texture,m.uniforms.resolution.value=R.mapSize,m.uniforms.radius.value=R.radius,i.setRenderTarget(R.map),i.clear(),i.renderBufferDirect(w,null,I,m,M,null)}function S(R,w,I,K){let g=null;const E=I.isPointLight===!0?R.customDistanceMaterial:R.customDepthMaterial;if(E!==void 0)g=E;else if(g=I.isPointLight===!0?l:o,i.localClippingEnabled&&w.clipShadows===!0&&Array.isArray(w.clippingPlanes)&&w.clippingPlanes.length!==0||w.displacementMap&&w.displacementScale!==0||w.alphaMap&&w.alphaTest>0||w.map&&w.alphaTest>0){const k=g.uuid,G=w.uuid;let X=c[k];X===void 0&&(X={},c[k]=X);let J=X[G];J===void 0&&(J=g.clone(),X[G]=J,w.addEventListener("dispose",N)),g=J}if(g.visible=w.visible,g.wireframe=w.wireframe,K===Jt?g.side=w.shadowSide!==null?w.shadowSide:w.side:g.side=w.shadowSide!==null?w.shadowSide:p[w.side],g.alphaMap=w.alphaMap,g.alphaTest=w.alphaTest,g.map=w.map,g.clipShadows=w.clipShadows,g.clippingPlanes=w.clippingPlanes,g.clipIntersection=w.clipIntersection,g.displacementMap=w.displacementMap,g.displacementScale=w.displacementScale,g.displacementBias=w.displacementBias,g.wireframeLinewidth=w.wireframeLinewidth,g.linewidth=w.linewidth,I.isPointLight===!0&&g.isMeshDistanceMaterial===!0){const k=i.properties.get(g);k.light=I}return g}function T(R,w,I,K,g){if(R.visible===!1)return;if(R.layers.test(w.layers)&&(R.isMesh||R.isLine||R.isPoints)&&(R.castShadow||R.receiveShadow&&g===Jt)&&(!R.frustumCulled||n.intersectsObject(R))){R.modelViewMatrix.multiplyMatrices(I.matrixWorldInverse,R.matrixWorld);const G=e.update(R),X=R.material;if(Array.isArray(X)){const J=G.groups;for(let H=0,Q=J.length;H<Q;H++){const W=J[H],le=X[W.materialIndex];if(le&&le.visible){const ce=S(R,le,K,g);R.onBeforeShadow(i,R,w,I,G,ce,W),i.renderBufferDirect(I,null,G,ce,R,W),R.onAfterShadow(i,R,w,I,G,ce,W)}}}else if(X.visible){const J=S(R,X,K,g);R.onBeforeShadow(i,R,w,I,G,J,null),i.renderBufferDirect(I,null,G,J,R,null),R.onAfterShadow(i,R,w,I,G,J,null)}}const k=R.children;for(let G=0,X=k.length;G<X;G++)T(k[G],w,I,K,g)}function N(R){R.target.removeEventListener("dispose",N);for(const I in c){const K=c[I],g=R.target.uuid;g in K&&(K[g].dispose(),delete K[g])}}}const ap={[as]:os,[ls]:hs,[cs]:ds,[jn]:us,[os]:as,[hs]:ls,[ds]:cs,[us]:jn};function op(i){function e(){let P=!1;const ue=new Ze;let V=null;const $=new Ze(0,0,0,0);return{setMask:function(ae){V!==ae&&!P&&(i.colorMask(ae,ae,ae,ae),V=ae)},setLocked:function(ae){P=ae},setClear:function(ae,he,Ge,rt,mt){mt===!0&&(ae*=rt,he*=rt,Ge*=rt),ue.set(ae,he,Ge,rt),$.equals(ue)===!1&&(i.clearColor(ae,he,Ge,rt),$.copy(ue))},reset:function(){P=!1,V=null,$.set(-1,0,0,0)}}}function t(){let P=!1,ue=!1,V=null,$=null,ae=null;return{setReversed:function(he){ue=he},setTest:function(he){he?ie(i.DEPTH_TEST):Z(i.DEPTH_TEST)},setMask:function(he){V!==he&&!P&&(i.depthMask(he),V=he)},setFunc:function(he){if(ue&&(he=ap[he]),$!==he){switch(he){case as:i.depthFunc(i.NEVER);break;case os:i.depthFunc(i.ALWAYS);break;case ls:i.depthFunc(i.LESS);break;case jn:i.depthFunc(i.LEQUAL);break;case cs:i.depthFunc(i.EQUAL);break;case us:i.depthFunc(i.GEQUAL);break;case hs:i.depthFunc(i.GREATER);break;case ds:i.depthFunc(i.NOTEQUAL);break;default:i.depthFunc(i.LEQUAL)}$=he}},setLocked:function(he){P=he},setClear:function(he){ae!==he&&(i.clearDepth(he),ae=he)},reset:function(){P=!1,V=null,$=null,ae=null}}}function n(){let P=!1,ue=null,V=null,$=null,ae=null,he=null,Ge=null,rt=null,mt=null;return{setTest:function(Xe){P||(Xe?ie(i.STENCIL_TEST):Z(i.STENCIL_TEST))},setMask:function(Xe){ue!==Xe&&!P&&(i.stencilMask(Xe),ue=Xe)},setFunc:function(Xe,_t,qt){(V!==Xe||$!==_t||ae!==qt)&&(i.stencilFunc(Xe,_t,qt),V=Xe,$=_t,ae=qt)},setOp:function(Xe,_t,qt){(he!==Xe||Ge!==_t||rt!==qt)&&(i.stencilOp(Xe,_t,qt),he=Xe,Ge=_t,rt=qt)},setLocked:function(Xe){P=Xe},setClear:function(Xe){mt!==Xe&&(i.clearStencil(Xe),mt=Xe)},reset:function(){P=!1,ue=null,V=null,$=null,ae=null,he=null,Ge=null,rt=null,mt=null}}}const r=new e,s=new t,a=new n,o=new WeakMap,l=new WeakMap;let c={},d={},p=new WeakMap,f=[],m=null,_=!1,M=null,h=null,u=null,y=null,S=null,T=null,N=null,R=new Oe(0,0,0),w=0,I=!1,K=null,g=null,E=null,k=null,G=null;const X=i.getParameter(i.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let J=!1,H=0;const Q=i.getParameter(i.VERSION);Q.indexOf("WebGL")!==-1?(H=parseFloat(/^WebGL (\d)/.exec(Q)[1]),J=H>=1):Q.indexOf("OpenGL ES")!==-1&&(H=parseFloat(/^OpenGL ES (\d)/.exec(Q)[1]),J=H>=2);let W=null,le={};const ce=i.getParameter(i.SCISSOR_BOX),_e=i.getParameter(i.VIEWPORT),He=new Ze().fromArray(ce),Ve=new Ze().fromArray(_e);function b(P,ue,V,$){const ae=new Uint8Array(4),he=i.createTexture();i.bindTexture(P,he),i.texParameteri(P,i.TEXTURE_MIN_FILTER,i.NEAREST),i.texParameteri(P,i.TEXTURE_MAG_FILTER,i.NEAREST);for(let Ge=0;Ge<V;Ge++)P===i.TEXTURE_3D||P===i.TEXTURE_2D_ARRAY?i.texImage3D(ue,0,i.RGBA,1,1,$,0,i.RGBA,i.UNSIGNED_BYTE,ae):i.texImage2D(ue+Ge,0,i.RGBA,1,1,0,i.RGBA,i.UNSIGNED_BYTE,ae);return he}const B={};B[i.TEXTURE_2D]=b(i.TEXTURE_2D,i.TEXTURE_2D,1),B[i.TEXTURE_CUBE_MAP]=b(i.TEXTURE_CUBE_MAP,i.TEXTURE_CUBE_MAP_POSITIVE_X,6),B[i.TEXTURE_2D_ARRAY]=b(i.TEXTURE_2D_ARRAY,i.TEXTURE_2D_ARRAY,1,1),B[i.TEXTURE_3D]=b(i.TEXTURE_3D,i.TEXTURE_3D,1,1),r.setClear(0,0,0,1),s.setClear(1),a.setClear(0),ie(i.DEPTH_TEST),s.setFunc(jn),Ue(!1),Ie(ma),ie(i.CULL_FACE),C(dn);function ie(P){c[P]!==!0&&(i.enable(P),c[P]=!0)}function Z(P){c[P]!==!1&&(i.disable(P),c[P]=!1)}function ge(P,ue){return d[P]!==ue?(i.bindFramebuffer(P,ue),d[P]=ue,P===i.DRAW_FRAMEBUFFER&&(d[i.FRAMEBUFFER]=ue),P===i.FRAMEBUFFER&&(d[i.DRAW_FRAMEBUFFER]=ue),!0):!1}function ee(P,ue){let V=f,$=!1;if(P){V=p.get(ue),V===void 0&&(V=[],p.set(ue,V));const ae=P.textures;if(V.length!==ae.length||V[0]!==i.COLOR_ATTACHMENT0){for(let he=0,Ge=ae.length;he<Ge;he++)V[he]=i.COLOR_ATTACHMENT0+he;V.length=ae.length,$=!0}}else V[0]!==i.BACK&&(V[0]=i.BACK,$=!0);$&&i.drawBuffers(V)}function be(P){return m!==P?(i.useProgram(P),m=P,!0):!1}const Ne={[bn]:i.FUNC_ADD,[El]:i.FUNC_SUBTRACT,[yl]:i.FUNC_REVERSE_SUBTRACT};Ne[Tl]=i.MIN,Ne[Al]=i.MAX;const Se={[bl]:i.ZERO,[wl]:i.ONE,[Rl]:i.SRC_COLOR,[rs]:i.SRC_ALPHA,[Il]:i.SRC_ALPHA_SATURATE,[Dl]:i.DST_COLOR,[Pl]:i.DST_ALPHA,[Cl]:i.ONE_MINUS_SRC_COLOR,[ss]:i.ONE_MINUS_SRC_ALPHA,[Ul]:i.ONE_MINUS_DST_COLOR,[Ll]:i.ONE_MINUS_DST_ALPHA,[Nl]:i.CONSTANT_COLOR,[Fl]:i.ONE_MINUS_CONSTANT_COLOR,[Ol]:i.CONSTANT_ALPHA,[Bl]:i.ONE_MINUS_CONSTANT_ALPHA};function C(P,ue,V,$,ae,he,Ge,rt,mt,Xe){if(P===dn){_===!0&&(Z(i.BLEND),_=!1);return}if(_===!1&&(ie(i.BLEND),_=!0),P!==Sl){if(P!==M||Xe!==I){if((h!==bn||S!==bn)&&(i.blendEquation(i.FUNC_ADD),h=bn,S=bn),Xe)switch(P){case Kn:i.blendFuncSeparate(i.ONE,i.ONE_MINUS_SRC_ALPHA,i.ONE,i.ONE_MINUS_SRC_ALPHA);break;case _a:i.blendFunc(i.ONE,i.ONE);break;case ga:i.blendFuncSeparate(i.ZERO,i.ONE_MINUS_SRC_COLOR,i.ZERO,i.ONE);break;case va:i.blendFuncSeparate(i.ZERO,i.SRC_COLOR,i.ZERO,i.SRC_ALPHA);break;default:console.error("THREE.WebGLState: Invalid blending: ",P);break}else switch(P){case Kn:i.blendFuncSeparate(i.SRC_ALPHA,i.ONE_MINUS_SRC_ALPHA,i.ONE,i.ONE_MINUS_SRC_ALPHA);break;case _a:i.blendFunc(i.SRC_ALPHA,i.ONE);break;case ga:i.blendFuncSeparate(i.ZERO,i.ONE_MINUS_SRC_COLOR,i.ZERO,i.ONE);break;case va:i.blendFunc(i.ZERO,i.SRC_COLOR);break;default:console.error("THREE.WebGLState: Invalid blending: ",P);break}u=null,y=null,T=null,N=null,R.set(0,0,0),w=0,M=P,I=Xe}return}ae=ae||ue,he=he||V,Ge=Ge||$,(ue!==h||ae!==S)&&(i.blendEquationSeparate(Ne[ue],Ne[ae]),h=ue,S=ae),(V!==u||$!==y||he!==T||Ge!==N)&&(i.blendFuncSeparate(Se[V],Se[$],Se[he],Se[Ge]),u=V,y=$,T=he,N=Ge),(rt.equals(R)===!1||mt!==w)&&(i.blendColor(rt.r,rt.g,rt.b,mt),R.copy(rt),w=mt),M=P,I=!1}function it(P,ue){P.side===Qt?Z(i.CULL_FACE):ie(i.CULL_FACE);let V=P.side===Mt;ue&&(V=!V),Ue(V),P.blending===Kn&&P.transparent===!1?C(dn):C(P.blending,P.blendEquation,P.blendSrc,P.blendDst,P.blendEquationAlpha,P.blendSrcAlpha,P.blendDstAlpha,P.blendColor,P.blendAlpha,P.premultipliedAlpha),s.setFunc(P.depthFunc),s.setTest(P.depthTest),s.setMask(P.depthWrite),r.setMask(P.colorWrite);const $=P.stencilWrite;a.setTest($),$&&(a.setMask(P.stencilWriteMask),a.setFunc(P.stencilFunc,P.stencilRef,P.stencilFuncMask),a.setOp(P.stencilFail,P.stencilZFail,P.stencilZPass)),ke(P.polygonOffset,P.polygonOffsetFactor,P.polygonOffsetUnits),P.alphaToCoverage===!0?ie(i.SAMPLE_ALPHA_TO_COVERAGE):Z(i.SAMPLE_ALPHA_TO_COVERAGE)}function Ue(P){K!==P&&(P?i.frontFace(i.CW):i.frontFace(i.CCW),K=P)}function Ie(P){P!==vl?(ie(i.CULL_FACE),P!==g&&(P===ma?i.cullFace(i.BACK):P===xl?i.cullFace(i.FRONT):i.cullFace(i.FRONT_AND_BACK))):Z(i.CULL_FACE),g=P}function Te(P){P!==E&&(J&&i.lineWidth(P),E=P)}function ke(P,ue,V){P?(ie(i.POLYGON_OFFSET_FILL),(k!==ue||G!==V)&&(i.polygonOffset(ue,V),k=ue,G=V)):Z(i.POLYGON_OFFSET_FILL)}function Ee(P){P?ie(i.SCISSOR_TEST):Z(i.SCISSOR_TEST)}function A(P){P===void 0&&(P=i.TEXTURE0+X-1),W!==P&&(i.activeTexture(P),W=P)}function v(P,ue,V){V===void 0&&(W===null?V=i.TEXTURE0+X-1:V=W);let $=le[V];$===void 0&&($={type:void 0,texture:void 0},le[V]=$),($.type!==P||$.texture!==ue)&&(W!==V&&(i.activeTexture(V),W=V),i.bindTexture(P,ue||B[P]),$.type=P,$.texture=ue)}function F(){const P=le[W];P!==void 0&&P.type!==void 0&&(i.bindTexture(P.type,null),P.type=void 0,P.texture=void 0)}function Y(){try{i.compressedTexImage2D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function j(){try{i.compressedTexImage3D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function q(){try{i.texSubImage2D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function xe(){try{i.texSubImage3D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function se(){try{i.compressedTexSubImage2D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function de(){try{i.compressedTexSubImage3D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function We(){try{i.texStorage2D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function te(){try{i.texStorage3D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function fe(){try{i.texImage2D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function Re(){try{i.texImage3D.apply(i,arguments)}catch(P){console.error("THREE.WebGLState:",P)}}function Ce(P){He.equals(P)===!1&&(i.scissor(P.x,P.y,P.z,P.w),He.copy(P))}function pe(P){Ve.equals(P)===!1&&(i.viewport(P.x,P.y,P.z,P.w),Ve.copy(P))}function Be(P,ue){let V=l.get(ue);V===void 0&&(V=new WeakMap,l.set(ue,V));let $=V.get(P);$===void 0&&($=i.getUniformBlockIndex(ue,P.name),V.set(P,$))}function Pe(P,ue){const $=l.get(ue).get(P);o.get(ue)!==$&&(i.uniformBlockBinding(ue,$,P.__bindingPointIndex),o.set(ue,$))}function je(){i.disable(i.BLEND),i.disable(i.CULL_FACE),i.disable(i.DEPTH_TEST),i.disable(i.POLYGON_OFFSET_FILL),i.disable(i.SCISSOR_TEST),i.disable(i.STENCIL_TEST),i.disable(i.SAMPLE_ALPHA_TO_COVERAGE),i.blendEquation(i.FUNC_ADD),i.blendFunc(i.ONE,i.ZERO),i.blendFuncSeparate(i.ONE,i.ZERO,i.ONE,i.ZERO),i.blendColor(0,0,0,0),i.colorMask(!0,!0,!0,!0),i.clearColor(0,0,0,0),i.depthMask(!0),i.depthFunc(i.LESS),i.clearDepth(1),i.stencilMask(4294967295),i.stencilFunc(i.ALWAYS,0,4294967295),i.stencilOp(i.KEEP,i.KEEP,i.KEEP),i.clearStencil(0),i.cullFace(i.BACK),i.frontFace(i.CCW),i.polygonOffset(0,0),i.activeTexture(i.TEXTURE0),i.bindFramebuffer(i.FRAMEBUFFER,null),i.bindFramebuffer(i.DRAW_FRAMEBUFFER,null),i.bindFramebuffer(i.READ_FRAMEBUFFER,null),i.useProgram(null),i.lineWidth(1),i.scissor(0,0,i.canvas.width,i.canvas.height),i.viewport(0,0,i.canvas.width,i.canvas.height),c={},W=null,le={},d={},p=new WeakMap,f=[],m=null,_=!1,M=null,h=null,u=null,y=null,S=null,T=null,N=null,R=new Oe(0,0,0),w=0,I=!1,K=null,g=null,E=null,k=null,G=null,He.set(0,0,i.canvas.width,i.canvas.height),Ve.set(0,0,i.canvas.width,i.canvas.height),r.reset(),s.reset(),a.reset()}return{buffers:{color:r,depth:s,stencil:a},enable:ie,disable:Z,bindFramebuffer:ge,drawBuffers:ee,useProgram:be,setBlending:C,setMaterial:it,setFlipSided:Ue,setCullFace:Ie,setLineWidth:Te,setPolygonOffset:ke,setScissorTest:Ee,activeTexture:A,bindTexture:v,unbindTexture:F,compressedTexImage2D:Y,compressedTexImage3D:j,texImage2D:fe,texImage3D:Re,updateUBOMapping:Be,uniformBlockBinding:Pe,texStorage2D:We,texStorage3D:te,texSubImage2D:q,texSubImage3D:xe,compressedTexSubImage2D:se,compressedTexSubImage3D:de,scissor:Ce,viewport:pe,reset:je}}function lo(i,e,t,n){const r=lp(n);switch(t){case To:return i*e;case bo:return i*e;case wo:return i*e*2;case Ro:return i*e/r.components*r.byteLength;case $s:return i*e/r.components*r.byteLength;case Co:return i*e*2/r.components*r.byteLength;case Zs:return i*e*2/r.components*r.byteLength;case Ao:return i*e*3/r.components*r.byteLength;case Ht:return i*e*4/r.components*r.byteLength;case js:return i*e*4/r.components*r.byteLength;case ji:case Ji:return Math.floor((i+3)/4)*Math.floor((e+3)/4)*8;case Qi:case er:return Math.floor((i+3)/4)*Math.floor((e+3)/4)*16;case vs:case Ms:return Math.max(i,16)*Math.max(e,8)/4;case gs:case xs:return Math.max(i,8)*Math.max(e,8)/2;case Ss:case Es:return Math.floor((i+3)/4)*Math.floor((e+3)/4)*8;case ys:return Math.floor((i+3)/4)*Math.floor((e+3)/4)*16;case Ts:return Math.floor((i+3)/4)*Math.floor((e+3)/4)*16;case As:return Math.floor((i+4)/5)*Math.floor((e+3)/4)*16;case bs:return Math.floor((i+4)/5)*Math.floor((e+4)/5)*16;case ws:return Math.floor((i+5)/6)*Math.floor((e+4)/5)*16;case Rs:return Math.floor((i+5)/6)*Math.floor((e+5)/6)*16;case Cs:return Math.floor((i+7)/8)*Math.floor((e+4)/5)*16;case Ps:return Math.floor((i+7)/8)*Math.floor((e+5)/6)*16;case Ls:return Math.floor((i+7)/8)*Math.floor((e+7)/8)*16;case Ds:return Math.floor((i+9)/10)*Math.floor((e+4)/5)*16;case Us:return Math.floor((i+9)/10)*Math.floor((e+5)/6)*16;case Is:return Math.floor((i+9)/10)*Math.floor((e+7)/8)*16;case Ns:return Math.floor((i+9)/10)*Math.floor((e+9)/10)*16;case Fs:return Math.floor((i+11)/12)*Math.floor((e+9)/10)*16;case Os:return Math.floor((i+11)/12)*Math.floor((e+11)/12)*16;case tr:case Bs:case zs:return Math.ceil(i/4)*Math.ceil(e/4)*16;case Po:case Hs:return Math.ceil(i/4)*Math.ceil(e/4)*8;case Gs:case Vs:return Math.ceil(i/4)*Math.ceil(e/4)*16}throw new Error(`Unable to determine texture byte length for ${t} format.`)}function lp(i){switch(i){case nn:case So:return{byteLength:1,components:1};case vi:case Eo:case xi:return{byteLength:2,components:1};case Ys:case Ks:return{byteLength:2,components:4};case Pn:case qs:case en:return{byteLength:4,components:1};case yo:return{byteLength:4,components:3}}throw new Error(`Unknown texture type ${i}.`)}function cp(i,e,t,n,r,s,a){const o=e.has("WEBGL_multisampled_render_to_texture")?e.get("WEBGL_multisampled_render_to_texture"):null,l=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),c=new ze,d=new WeakMap;let p;const f=new WeakMap;let m=!1;try{m=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function _(A,v){return m?new OffscreenCanvas(A,v):lr("canvas")}function M(A,v,F){let Y=1;const j=Ee(A);if((j.width>F||j.height>F)&&(Y=F/Math.max(j.width,j.height)),Y<1)if(typeof HTMLImageElement<"u"&&A instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&A instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&A instanceof ImageBitmap||typeof VideoFrame<"u"&&A instanceof VideoFrame){const q=Math.floor(Y*j.width),xe=Math.floor(Y*j.height);p===void 0&&(p=_(q,xe));const se=v?_(q,xe):p;return se.width=q,se.height=xe,se.getContext("2d").drawImage(A,0,0,q,xe),console.warn("THREE.WebGLRenderer: Texture has been resized from ("+j.width+"x"+j.height+") to ("+q+"x"+xe+")."),se}else return"data"in A&&console.warn("THREE.WebGLRenderer: Image in DataTexture is too big ("+j.width+"x"+j.height+")."),A;return A}function h(A){return A.generateMipmaps&&A.minFilter!==Lt&&A.minFilter!==Bt}function u(A){i.generateMipmap(A)}function y(A,v,F,Y,j=!1){if(A!==null){if(i[A]!==void 0)return i[A];console.warn("THREE.WebGLRenderer: Attempt to use non-existing WebGL internal format '"+A+"'")}let q=v;if(v===i.RED&&(F===i.FLOAT&&(q=i.R32F),F===i.HALF_FLOAT&&(q=i.R16F),F===i.UNSIGNED_BYTE&&(q=i.R8)),v===i.RED_INTEGER&&(F===i.UNSIGNED_BYTE&&(q=i.R8UI),F===i.UNSIGNED_SHORT&&(q=i.R16UI),F===i.UNSIGNED_INT&&(q=i.R32UI),F===i.BYTE&&(q=i.R8I),F===i.SHORT&&(q=i.R16I),F===i.INT&&(q=i.R32I)),v===i.RG&&(F===i.FLOAT&&(q=i.RG32F),F===i.HALF_FLOAT&&(q=i.RG16F),F===i.UNSIGNED_BYTE&&(q=i.RG8)),v===i.RG_INTEGER&&(F===i.UNSIGNED_BYTE&&(q=i.RG8UI),F===i.UNSIGNED_SHORT&&(q=i.RG16UI),F===i.UNSIGNED_INT&&(q=i.RG32UI),F===i.BYTE&&(q=i.RG8I),F===i.SHORT&&(q=i.RG16I),F===i.INT&&(q=i.RG32I)),v===i.RGB_INTEGER&&(F===i.UNSIGNED_BYTE&&(q=i.RGB8UI),F===i.UNSIGNED_SHORT&&(q=i.RGB16UI),F===i.UNSIGNED_INT&&(q=i.RGB32UI),F===i.BYTE&&(q=i.RGB8I),F===i.SHORT&&(q=i.RGB16I),F===i.INT&&(q=i.RGB32I)),v===i.RGBA_INTEGER&&(F===i.UNSIGNED_BYTE&&(q=i.RGBA8UI),F===i.UNSIGNED_SHORT&&(q=i.RGBA16UI),F===i.UNSIGNED_INT&&(q=i.RGBA32UI),F===i.BYTE&&(q=i.RGBA8I),F===i.SHORT&&(q=i.RGBA16I),F===i.INT&&(q=i.RGBA32I)),v===i.RGB&&F===i.UNSIGNED_INT_5_9_9_9_REV&&(q=i.RGB9_E5),v===i.RGBA){const xe=j?rr:Ke.getTransfer(Y);F===i.FLOAT&&(q=i.RGBA32F),F===i.HALF_FLOAT&&(q=i.RGBA16F),F===i.UNSIGNED_BYTE&&(q=xe===Qe?i.SRGB8_ALPHA8:i.RGBA8),F===i.UNSIGNED_SHORT_4_4_4_4&&(q=i.RGBA4),F===i.UNSIGNED_SHORT_5_5_5_1&&(q=i.RGB5_A1)}return(q===i.R16F||q===i.R32F||q===i.RG16F||q===i.RG32F||q===i.RGBA16F||q===i.RGBA32F)&&e.get("EXT_color_buffer_float"),q}function S(A,v){let F;return A?v===null||v===Pn||v===ei?F=i.DEPTH24_STENCIL8:v===en?F=i.DEPTH32F_STENCIL8:v===vi&&(F=i.DEPTH24_STENCIL8,console.warn("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):v===null||v===Pn||v===ei?F=i.DEPTH_COMPONENT24:v===en?F=i.DEPTH_COMPONENT32F:v===vi&&(F=i.DEPTH_COMPONENT16),F}function T(A,v){return h(A)===!0||A.isFramebufferTexture&&A.minFilter!==Lt&&A.minFilter!==Bt?Math.log2(Math.max(v.width,v.height))+1:A.mipmaps!==void 0&&A.mipmaps.length>0?A.mipmaps.length:A.isCompressedTexture&&Array.isArray(A.image)?v.mipmaps.length:1}function N(A){const v=A.target;v.removeEventListener("dispose",N),w(v),v.isVideoTexture&&d.delete(v)}function R(A){const v=A.target;v.removeEventListener("dispose",R),K(v)}function w(A){const v=n.get(A);if(v.__webglInit===void 0)return;const F=A.source,Y=f.get(F);if(Y){const j=Y[v.__cacheKey];j.usedTimes--,j.usedTimes===0&&I(A),Object.keys(Y).length===0&&f.delete(F)}n.remove(A)}function I(A){const v=n.get(A);i.deleteTexture(v.__webglTexture);const F=A.source,Y=f.get(F);delete Y[v.__cacheKey],a.memory.textures--}function K(A){const v=n.get(A);if(A.depthTexture&&A.depthTexture.dispose(),A.isWebGLCubeRenderTarget)for(let Y=0;Y<6;Y++){if(Array.isArray(v.__webglFramebuffer[Y]))for(let j=0;j<v.__webglFramebuffer[Y].length;j++)i.deleteFramebuffer(v.__webglFramebuffer[Y][j]);else i.deleteFramebuffer(v.__webglFramebuffer[Y]);v.__webglDepthbuffer&&i.deleteRenderbuffer(v.__webglDepthbuffer[Y])}else{if(Array.isArray(v.__webglFramebuffer))for(let Y=0;Y<v.__webglFramebuffer.length;Y++)i.deleteFramebuffer(v.__webglFramebuffer[Y]);else i.deleteFramebuffer(v.__webglFramebuffer);if(v.__webglDepthbuffer&&i.deleteRenderbuffer(v.__webglDepthbuffer),v.__webglMultisampledFramebuffer&&i.deleteFramebuffer(v.__webglMultisampledFramebuffer),v.__webglColorRenderbuffer)for(let Y=0;Y<v.__webglColorRenderbuffer.length;Y++)v.__webglColorRenderbuffer[Y]&&i.deleteRenderbuffer(v.__webglColorRenderbuffer[Y]);v.__webglDepthRenderbuffer&&i.deleteRenderbuffer(v.__webglDepthRenderbuffer)}const F=A.textures;for(let Y=0,j=F.length;Y<j;Y++){const q=n.get(F[Y]);q.__webglTexture&&(i.deleteTexture(q.__webglTexture),a.memory.textures--),n.remove(F[Y])}n.remove(A)}let g=0;function E(){g=0}function k(){const A=g;return A>=r.maxTextures&&console.warn("THREE.WebGLTextures: Trying to use "+A+" texture units while this GPU supports only "+r.maxTextures),g+=1,A}function G(A){const v=[];return v.push(A.wrapS),v.push(A.wrapT),v.push(A.wrapR||0),v.push(A.magFilter),v.push(A.minFilter),v.push(A.anisotropy),v.push(A.internalFormat),v.push(A.format),v.push(A.type),v.push(A.generateMipmaps),v.push(A.premultiplyAlpha),v.push(A.flipY),v.push(A.unpackAlignment),v.push(A.colorSpace),v.join()}function X(A,v){const F=n.get(A);if(A.isVideoTexture&&Te(A),A.isRenderTargetTexture===!1&&A.version>0&&F.__version!==A.version){const Y=A.image;if(Y===null)console.warn("THREE.WebGLRenderer: Texture marked for update but no image data found.");else if(Y.complete===!1)console.warn("THREE.WebGLRenderer: Texture marked for update but image is incomplete");else{Ve(F,A,v);return}}t.bindTexture(i.TEXTURE_2D,F.__webglTexture,i.TEXTURE0+v)}function J(A,v){const F=n.get(A);if(A.version>0&&F.__version!==A.version){Ve(F,A,v);return}t.bindTexture(i.TEXTURE_2D_ARRAY,F.__webglTexture,i.TEXTURE0+v)}function H(A,v){const F=n.get(A);if(A.version>0&&F.__version!==A.version){Ve(F,A,v);return}t.bindTexture(i.TEXTURE_3D,F.__webglTexture,i.TEXTURE0+v)}function Q(A,v){const F=n.get(A);if(A.version>0&&F.__version!==A.version){b(F,A,v);return}t.bindTexture(i.TEXTURE_CUBE_MAP,F.__webglTexture,i.TEXTURE0+v)}const W={[ms]:i.REPEAT,[Rn]:i.CLAMP_TO_EDGE,[_s]:i.MIRRORED_REPEAT},le={[Lt]:i.NEAREST,[Kl]:i.NEAREST_MIPMAP_NEAREST,[Ri]:i.NEAREST_MIPMAP_LINEAR,[Bt]:i.LINEAR,[Tr]:i.LINEAR_MIPMAP_NEAREST,[Cn]:i.LINEAR_MIPMAP_LINEAR},ce={[Jl]:i.NEVER,[rc]:i.ALWAYS,[Ql]:i.LESS,[Do]:i.LEQUAL,[ec]:i.EQUAL,[ic]:i.GEQUAL,[tc]:i.GREATER,[nc]:i.NOTEQUAL};function _e(A,v){if(v.type===en&&e.has("OES_texture_float_linear")===!1&&(v.magFilter===Bt||v.magFilter===Tr||v.magFilter===Ri||v.magFilter===Cn||v.minFilter===Bt||v.minFilter===Tr||v.minFilter===Ri||v.minFilter===Cn)&&console.warn("THREE.WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),i.texParameteri(A,i.TEXTURE_WRAP_S,W[v.wrapS]),i.texParameteri(A,i.TEXTURE_WRAP_T,W[v.wrapT]),(A===i.TEXTURE_3D||A===i.TEXTURE_2D_ARRAY)&&i.texParameteri(A,i.TEXTURE_WRAP_R,W[v.wrapR]),i.texParameteri(A,i.TEXTURE_MAG_FILTER,le[v.magFilter]),i.texParameteri(A,i.TEXTURE_MIN_FILTER,le[v.minFilter]),v.compareFunction&&(i.texParameteri(A,i.TEXTURE_COMPARE_MODE,i.COMPARE_REF_TO_TEXTURE),i.texParameteri(A,i.TEXTURE_COMPARE_FUNC,ce[v.compareFunction])),e.has("EXT_texture_filter_anisotropic")===!0){if(v.magFilter===Lt||v.minFilter!==Ri&&v.minFilter!==Cn||v.type===en&&e.has("OES_texture_float_linear")===!1)return;if(v.anisotropy>1||n.get(v).__currentAnisotropy){const F=e.get("EXT_texture_filter_anisotropic");i.texParameterf(A,F.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(v.anisotropy,r.getMaxAnisotropy())),n.get(v).__currentAnisotropy=v.anisotropy}}}function He(A,v){let F=!1;A.__webglInit===void 0&&(A.__webglInit=!0,v.addEventListener("dispose",N));const Y=v.source;let j=f.get(Y);j===void 0&&(j={},f.set(Y,j));const q=G(v);if(q!==A.__cacheKey){j[q]===void 0&&(j[q]={texture:i.createTexture(),usedTimes:0},a.memory.textures++,F=!0),j[q].usedTimes++;const xe=j[A.__cacheKey];xe!==void 0&&(j[A.__cacheKey].usedTimes--,xe.usedTimes===0&&I(v)),A.__cacheKey=q,A.__webglTexture=j[q].texture}return F}function Ve(A,v,F){let Y=i.TEXTURE_2D;(v.isDataArrayTexture||v.isCompressedArrayTexture)&&(Y=i.TEXTURE_2D_ARRAY),v.isData3DTexture&&(Y=i.TEXTURE_3D);const j=He(A,v),q=v.source;t.bindTexture(Y,A.__webglTexture,i.TEXTURE0+F);const xe=n.get(q);if(q.version!==xe.__version||j===!0){t.activeTexture(i.TEXTURE0+F);const se=Ke.getPrimaries(Ke.workingColorSpace),de=v.colorSpace===hn?null:Ke.getPrimaries(v.colorSpace),We=v.colorSpace===hn||se===de?i.NONE:i.BROWSER_DEFAULT_WEBGL;i.pixelStorei(i.UNPACK_FLIP_Y_WEBGL,v.flipY),i.pixelStorei(i.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),i.pixelStorei(i.UNPACK_ALIGNMENT,v.unpackAlignment),i.pixelStorei(i.UNPACK_COLORSPACE_CONVERSION_WEBGL,We);let te=M(v.image,!1,r.maxTextureSize);te=ke(v,te);const fe=s.convert(v.format,v.colorSpace),Re=s.convert(v.type);let Ce=y(v.internalFormat,fe,Re,v.colorSpace,v.isVideoTexture);_e(Y,v);let pe;const Be=v.mipmaps,Pe=v.isVideoTexture!==!0,je=xe.__version===void 0||j===!0,P=q.dataReady,ue=T(v,te);if(v.isDepthTexture)Ce=S(v.format===ti,v.type),je&&(Pe?t.texStorage2D(i.TEXTURE_2D,1,Ce,te.width,te.height):t.texImage2D(i.TEXTURE_2D,0,Ce,te.width,te.height,0,fe,Re,null));else if(v.isDataTexture)if(Be.length>0){Pe&&je&&t.texStorage2D(i.TEXTURE_2D,ue,Ce,Be[0].width,Be[0].height);for(let V=0,$=Be.length;V<$;V++)pe=Be[V],Pe?P&&t.texSubImage2D(i.TEXTURE_2D,V,0,0,pe.width,pe.height,fe,Re,pe.data):t.texImage2D(i.TEXTURE_2D,V,Ce,pe.width,pe.height,0,fe,Re,pe.data);v.generateMipmaps=!1}else Pe?(je&&t.texStorage2D(i.TEXTURE_2D,ue,Ce,te.width,te.height),P&&t.texSubImage2D(i.TEXTURE_2D,0,0,0,te.width,te.height,fe,Re,te.data)):t.texImage2D(i.TEXTURE_2D,0,Ce,te.width,te.height,0,fe,Re,te.data);else if(v.isCompressedTexture)if(v.isCompressedArrayTexture){Pe&&je&&t.texStorage3D(i.TEXTURE_2D_ARRAY,ue,Ce,Be[0].width,Be[0].height,te.depth);for(let V=0,$=Be.length;V<$;V++)if(pe=Be[V],v.format!==Ht)if(fe!==null)if(Pe){if(P)if(v.layerUpdates.size>0){const ae=lo(pe.width,pe.height,v.format,v.type);for(const he of v.layerUpdates){const Ge=pe.data.subarray(he*ae/pe.data.BYTES_PER_ELEMENT,(he+1)*ae/pe.data.BYTES_PER_ELEMENT);t.compressedTexSubImage3D(i.TEXTURE_2D_ARRAY,V,0,0,he,pe.width,pe.height,1,fe,Ge,0,0)}v.clearLayerUpdates()}else t.compressedTexSubImage3D(i.TEXTURE_2D_ARRAY,V,0,0,0,pe.width,pe.height,te.depth,fe,pe.data,0,0)}else t.compressedTexImage3D(i.TEXTURE_2D_ARRAY,V,Ce,pe.width,pe.height,te.depth,0,pe.data,0,0);else console.warn("THREE.WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else Pe?P&&t.texSubImage3D(i.TEXTURE_2D_ARRAY,V,0,0,0,pe.width,pe.height,te.depth,fe,Re,pe.data):t.texImage3D(i.TEXTURE_2D_ARRAY,V,Ce,pe.width,pe.height,te.depth,0,fe,Re,pe.data)}else{Pe&&je&&t.texStorage2D(i.TEXTURE_2D,ue,Ce,Be[0].width,Be[0].height);for(let V=0,$=Be.length;V<$;V++)pe=Be[V],v.format!==Ht?fe!==null?Pe?P&&t.compressedTexSubImage2D(i.TEXTURE_2D,V,0,0,pe.width,pe.height,fe,pe.data):t.compressedTexImage2D(i.TEXTURE_2D,V,Ce,pe.width,pe.height,0,pe.data):console.warn("THREE.WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):Pe?P&&t.texSubImage2D(i.TEXTURE_2D,V,0,0,pe.width,pe.height,fe,Re,pe.data):t.texImage2D(i.TEXTURE_2D,V,Ce,pe.width,pe.height,0,fe,Re,pe.data)}else if(v.isDataArrayTexture)if(Pe){if(je&&t.texStorage3D(i.TEXTURE_2D_ARRAY,ue,Ce,te.width,te.height,te.depth),P)if(v.layerUpdates.size>0){const V=lo(te.width,te.height,v.format,v.type);for(const $ of v.layerUpdates){const ae=te.data.subarray($*V/te.data.BYTES_PER_ELEMENT,($+1)*V/te.data.BYTES_PER_ELEMENT);t.texSubImage3D(i.TEXTURE_2D_ARRAY,0,0,0,$,te.width,te.height,1,fe,Re,ae)}v.clearLayerUpdates()}else t.texSubImage3D(i.TEXTURE_2D_ARRAY,0,0,0,0,te.width,te.height,te.depth,fe,Re,te.data)}else t.texImage3D(i.TEXTURE_2D_ARRAY,0,Ce,te.width,te.height,te.depth,0,fe,Re,te.data);else if(v.isData3DTexture)Pe?(je&&t.texStorage3D(i.TEXTURE_3D,ue,Ce,te.width,te.height,te.depth),P&&t.texSubImage3D(i.TEXTURE_3D,0,0,0,0,te.width,te.height,te.depth,fe,Re,te.data)):t.texImage3D(i.TEXTURE_3D,0,Ce,te.width,te.height,te.depth,0,fe,Re,te.data);else if(v.isFramebufferTexture){if(je)if(Pe)t.texStorage2D(i.TEXTURE_2D,ue,Ce,te.width,te.height);else{let V=te.width,$=te.height;for(let ae=0;ae<ue;ae++)t.texImage2D(i.TEXTURE_2D,ae,Ce,V,$,0,fe,Re,null),V>>=1,$>>=1}}else if(Be.length>0){if(Pe&&je){const V=Ee(Be[0]);t.texStorage2D(i.TEXTURE_2D,ue,Ce,V.width,V.height)}for(let V=0,$=Be.length;V<$;V++)pe=Be[V],Pe?P&&t.texSubImage2D(i.TEXTURE_2D,V,0,0,fe,Re,pe):t.texImage2D(i.TEXTURE_2D,V,Ce,fe,Re,pe);v.generateMipmaps=!1}else if(Pe){if(je){const V=Ee(te);t.texStorage2D(i.TEXTURE_2D,ue,Ce,V.width,V.height)}P&&t.texSubImage2D(i.TEXTURE_2D,0,0,0,fe,Re,te)}else t.texImage2D(i.TEXTURE_2D,0,Ce,fe,Re,te);h(v)&&u(Y),xe.__version=q.version,v.onUpdate&&v.onUpdate(v)}A.__version=v.version}function b(A,v,F){if(v.image.length!==6)return;const Y=He(A,v),j=v.source;t.bindTexture(i.TEXTURE_CUBE_MAP,A.__webglTexture,i.TEXTURE0+F);const q=n.get(j);if(j.version!==q.__version||Y===!0){t.activeTexture(i.TEXTURE0+F);const xe=Ke.getPrimaries(Ke.workingColorSpace),se=v.colorSpace===hn?null:Ke.getPrimaries(v.colorSpace),de=v.colorSpace===hn||xe===se?i.NONE:i.BROWSER_DEFAULT_WEBGL;i.pixelStorei(i.UNPACK_FLIP_Y_WEBGL,v.flipY),i.pixelStorei(i.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),i.pixelStorei(i.UNPACK_ALIGNMENT,v.unpackAlignment),i.pixelStorei(i.UNPACK_COLORSPACE_CONVERSION_WEBGL,de);const We=v.isCompressedTexture||v.image[0].isCompressedTexture,te=v.image[0]&&v.image[0].isDataTexture,fe=[];for(let $=0;$<6;$++)!We&&!te?fe[$]=M(v.image[$],!0,r.maxCubemapSize):fe[$]=te?v.image[$].image:v.image[$],fe[$]=ke(v,fe[$]);const Re=fe[0],Ce=s.convert(v.format,v.colorSpace),pe=s.convert(v.type),Be=y(v.internalFormat,Ce,pe,v.colorSpace),Pe=v.isVideoTexture!==!0,je=q.__version===void 0||Y===!0,P=j.dataReady;let ue=T(v,Re);_e(i.TEXTURE_CUBE_MAP,v);let V;if(We){Pe&&je&&t.texStorage2D(i.TEXTURE_CUBE_MAP,ue,Be,Re.width,Re.height);for(let $=0;$<6;$++){V=fe[$].mipmaps;for(let ae=0;ae<V.length;ae++){const he=V[ae];v.format!==Ht?Ce!==null?Pe?P&&t.compressedTexSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae,0,0,he.width,he.height,Ce,he.data):t.compressedTexImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae,Be,he.width,he.height,0,he.data):console.warn("THREE.WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):Pe?P&&t.texSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae,0,0,he.width,he.height,Ce,pe,he.data):t.texImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae,Be,he.width,he.height,0,Ce,pe,he.data)}}}else{if(V=v.mipmaps,Pe&&je){V.length>0&&ue++;const $=Ee(fe[0]);t.texStorage2D(i.TEXTURE_CUBE_MAP,ue,Be,$.width,$.height)}for(let $=0;$<6;$++)if(te){Pe?P&&t.texSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,0,0,fe[$].width,fe[$].height,Ce,pe,fe[$].data):t.texImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,Be,fe[$].width,fe[$].height,0,Ce,pe,fe[$].data);for(let ae=0;ae<V.length;ae++){const Ge=V[ae].image[$].image;Pe?P&&t.texSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae+1,0,0,Ge.width,Ge.height,Ce,pe,Ge.data):t.texImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae+1,Be,Ge.width,Ge.height,0,Ce,pe,Ge.data)}}else{Pe?P&&t.texSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,0,0,Ce,pe,fe[$]):t.texImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,Be,Ce,pe,fe[$]);for(let ae=0;ae<V.length;ae++){const he=V[ae];Pe?P&&t.texSubImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae+1,0,0,Ce,pe,he.image[$]):t.texImage2D(i.TEXTURE_CUBE_MAP_POSITIVE_X+$,ae+1,Be,Ce,pe,he.image[$])}}}h(v)&&u(i.TEXTURE_CUBE_MAP),q.__version=j.version,v.onUpdate&&v.onUpdate(v)}A.__version=v.version}function B(A,v,F,Y,j,q){const xe=s.convert(F.format,F.colorSpace),se=s.convert(F.type),de=y(F.internalFormat,xe,se,F.colorSpace);if(!n.get(v).__hasExternalTextures){const te=Math.max(1,v.width>>q),fe=Math.max(1,v.height>>q);j===i.TEXTURE_3D||j===i.TEXTURE_2D_ARRAY?t.texImage3D(j,q,de,te,fe,v.depth,0,xe,se,null):t.texImage2D(j,q,de,te,fe,0,xe,se,null)}t.bindFramebuffer(i.FRAMEBUFFER,A),Ie(v)?o.framebufferTexture2DMultisampleEXT(i.FRAMEBUFFER,Y,j,n.get(F).__webglTexture,0,Ue(v)):(j===i.TEXTURE_2D||j>=i.TEXTURE_CUBE_MAP_POSITIVE_X&&j<=i.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&i.framebufferTexture2D(i.FRAMEBUFFER,Y,j,n.get(F).__webglTexture,q),t.bindFramebuffer(i.FRAMEBUFFER,null)}function ie(A,v,F){if(i.bindRenderbuffer(i.RENDERBUFFER,A),v.depthBuffer){const Y=v.depthTexture,j=Y&&Y.isDepthTexture?Y.type:null,q=S(v.stencilBuffer,j),xe=v.stencilBuffer?i.DEPTH_STENCIL_ATTACHMENT:i.DEPTH_ATTACHMENT,se=Ue(v);Ie(v)?o.renderbufferStorageMultisampleEXT(i.RENDERBUFFER,se,q,v.width,v.height):F?i.renderbufferStorageMultisample(i.RENDERBUFFER,se,q,v.width,v.height):i.renderbufferStorage(i.RENDERBUFFER,q,v.width,v.height),i.framebufferRenderbuffer(i.FRAMEBUFFER,xe,i.RENDERBUFFER,A)}else{const Y=v.textures;for(let j=0;j<Y.length;j++){const q=Y[j],xe=s.convert(q.format,q.colorSpace),se=s.convert(q.type),de=y(q.internalFormat,xe,se,q.colorSpace),We=Ue(v);F&&Ie(v)===!1?i.renderbufferStorageMultisample(i.RENDERBUFFER,We,de,v.width,v.height):Ie(v)?o.renderbufferStorageMultisampleEXT(i.RENDERBUFFER,We,de,v.width,v.height):i.renderbufferStorage(i.RENDERBUFFER,de,v.width,v.height)}}i.bindRenderbuffer(i.RENDERBUFFER,null)}function Z(A,v){if(v&&v.isWebGLCubeRenderTarget)throw new Error("Depth Texture with cube render targets is not supported");if(t.bindFramebuffer(i.FRAMEBUFFER,A),!(v.depthTexture&&v.depthTexture.isDepthTexture))throw new Error("renderTarget.depthTexture must be an instance of THREE.DepthTexture");(!n.get(v.depthTexture).__webglTexture||v.depthTexture.image.width!==v.width||v.depthTexture.image.height!==v.height)&&(v.depthTexture.image.width=v.width,v.depthTexture.image.height=v.height,v.depthTexture.needsUpdate=!0),X(v.depthTexture,0);const Y=n.get(v.depthTexture).__webglTexture,j=Ue(v);if(v.depthTexture.format===$n)Ie(v)?o.framebufferTexture2DMultisampleEXT(i.FRAMEBUFFER,i.DEPTH_ATTACHMENT,i.TEXTURE_2D,Y,0,j):i.framebufferTexture2D(i.FRAMEBUFFER,i.DEPTH_ATTACHMENT,i.TEXTURE_2D,Y,0);else if(v.depthTexture.format===ti)Ie(v)?o.framebufferTexture2DMultisampleEXT(i.FRAMEBUFFER,i.DEPTH_STENCIL_ATTACHMENT,i.TEXTURE_2D,Y,0,j):i.framebufferTexture2D(i.FRAMEBUFFER,i.DEPTH_STENCIL_ATTACHMENT,i.TEXTURE_2D,Y,0);else throw new Error("Unknown depthTexture format")}function ge(A){const v=n.get(A),F=A.isWebGLCubeRenderTarget===!0;if(v.__boundDepthTexture!==A.depthTexture){const Y=A.depthTexture;if(v.__depthDisposeCallback&&v.__depthDisposeCallback(),Y){const j=()=>{delete v.__boundDepthTexture,delete v.__depthDisposeCallback,Y.removeEventListener("dispose",j)};Y.addEventListener("dispose",j),v.__depthDisposeCallback=j}v.__boundDepthTexture=Y}if(A.depthTexture&&!v.__autoAllocateDepthBuffer){if(F)throw new Error("target.depthTexture not supported in Cube render targets");Z(v.__webglFramebuffer,A)}else if(F){v.__webglDepthbuffer=[];for(let Y=0;Y<6;Y++)if(t.bindFramebuffer(i.FRAMEBUFFER,v.__webglFramebuffer[Y]),v.__webglDepthbuffer[Y]===void 0)v.__webglDepthbuffer[Y]=i.createRenderbuffer(),ie(v.__webglDepthbuffer[Y],A,!1);else{const j=A.stencilBuffer?i.DEPTH_STENCIL_ATTACHMENT:i.DEPTH_ATTACHMENT,q=v.__webglDepthbuffer[Y];i.bindRenderbuffer(i.RENDERBUFFER,q),i.framebufferRenderbuffer(i.FRAMEBUFFER,j,i.RENDERBUFFER,q)}}else if(t.bindFramebuffer(i.FRAMEBUFFER,v.__webglFramebuffer),v.__webglDepthbuffer===void 0)v.__webglDepthbuffer=i.createRenderbuffer(),ie(v.__webglDepthbuffer,A,!1);else{const Y=A.stencilBuffer?i.DEPTH_STENCIL_ATTACHMENT:i.DEPTH_ATTACHMENT,j=v.__webglDepthbuffer;i.bindRenderbuffer(i.RENDERBUFFER,j),i.framebufferRenderbuffer(i.FRAMEBUFFER,Y,i.RENDERBUFFER,j)}t.bindFramebuffer(i.FRAMEBUFFER,null)}function ee(A,v,F){const Y=n.get(A);v!==void 0&&B(Y.__webglFramebuffer,A,A.texture,i.COLOR_ATTACHMENT0,i.TEXTURE_2D,0),F!==void 0&&ge(A)}function be(A){const v=A.texture,F=n.get(A),Y=n.get(v);A.addEventListener("dispose",R);const j=A.textures,q=A.isWebGLCubeRenderTarget===!0,xe=j.length>1;if(xe||(Y.__webglTexture===void 0&&(Y.__webglTexture=i.createTexture()),Y.__version=v.version,a.memory.textures++),q){F.__webglFramebuffer=[];for(let se=0;se<6;se++)if(v.mipmaps&&v.mipmaps.length>0){F.__webglFramebuffer[se]=[];for(let de=0;de<v.mipmaps.length;de++)F.__webglFramebuffer[se][de]=i.createFramebuffer()}else F.__webglFramebuffer[se]=i.createFramebuffer()}else{if(v.mipmaps&&v.mipmaps.length>0){F.__webglFramebuffer=[];for(let se=0;se<v.mipmaps.length;se++)F.__webglFramebuffer[se]=i.createFramebuffer()}else F.__webglFramebuffer=i.createFramebuffer();if(xe)for(let se=0,de=j.length;se<de;se++){const We=n.get(j[se]);We.__webglTexture===void 0&&(We.__webglTexture=i.createTexture(),a.memory.textures++)}if(A.samples>0&&Ie(A)===!1){F.__webglMultisampledFramebuffer=i.createFramebuffer(),F.__webglColorRenderbuffer=[],t.bindFramebuffer(i.FRAMEBUFFER,F.__webglMultisampledFramebuffer);for(let se=0;se<j.length;se++){const de=j[se];F.__webglColorRenderbuffer[se]=i.createRenderbuffer(),i.bindRenderbuffer(i.RENDERBUFFER,F.__webglColorRenderbuffer[se]);const We=s.convert(de.format,de.colorSpace),te=s.convert(de.type),fe=y(de.internalFormat,We,te,de.colorSpace,A.isXRRenderTarget===!0),Re=Ue(A);i.renderbufferStorageMultisample(i.RENDERBUFFER,Re,fe,A.width,A.height),i.framebufferRenderbuffer(i.FRAMEBUFFER,i.COLOR_ATTACHMENT0+se,i.RENDERBUFFER,F.__webglColorRenderbuffer[se])}i.bindRenderbuffer(i.RENDERBUFFER,null),A.depthBuffer&&(F.__webglDepthRenderbuffer=i.createRenderbuffer(),ie(F.__webglDepthRenderbuffer,A,!0)),t.bindFramebuffer(i.FRAMEBUFFER,null)}}if(q){t.bindTexture(i.TEXTURE_CUBE_MAP,Y.__webglTexture),_e(i.TEXTURE_CUBE_MAP,v);for(let se=0;se<6;se++)if(v.mipmaps&&v.mipmaps.length>0)for(let de=0;de<v.mipmaps.length;de++)B(F.__webglFramebuffer[se][de],A,v,i.COLOR_ATTACHMENT0,i.TEXTURE_CUBE_MAP_POSITIVE_X+se,de);else B(F.__webglFramebuffer[se],A,v,i.COLOR_ATTACHMENT0,i.TEXTURE_CUBE_MAP_POSITIVE_X+se,0);h(v)&&u(i.TEXTURE_CUBE_MAP),t.unbindTexture()}else if(xe){for(let se=0,de=j.length;se<de;se++){const We=j[se],te=n.get(We);t.bindTexture(i.TEXTURE_2D,te.__webglTexture),_e(i.TEXTURE_2D,We),B(F.__webglFramebuffer,A,We,i.COLOR_ATTACHMENT0+se,i.TEXTURE_2D,0),h(We)&&u(i.TEXTURE_2D)}t.unbindTexture()}else{let se=i.TEXTURE_2D;if((A.isWebGL3DRenderTarget||A.isWebGLArrayRenderTarget)&&(se=A.isWebGL3DRenderTarget?i.TEXTURE_3D:i.TEXTURE_2D_ARRAY),t.bindTexture(se,Y.__webglTexture),_e(se,v),v.mipmaps&&v.mipmaps.length>0)for(let de=0;de<v.mipmaps.length;de++)B(F.__webglFramebuffer[de],A,v,i.COLOR_ATTACHMENT0,se,de);else B(F.__webglFramebuffer,A,v,i.COLOR_ATTACHMENT0,se,0);h(v)&&u(se),t.unbindTexture()}A.depthBuffer&&ge(A)}function Ne(A){const v=A.textures;for(let F=0,Y=v.length;F<Y;F++){const j=v[F];if(h(j)){const q=A.isWebGLCubeRenderTarget?i.TEXTURE_CUBE_MAP:i.TEXTURE_2D,xe=n.get(j).__webglTexture;t.bindTexture(q,xe),u(q),t.unbindTexture()}}}const Se=[],C=[];function it(A){if(A.samples>0){if(Ie(A)===!1){const v=A.textures,F=A.width,Y=A.height;let j=i.COLOR_BUFFER_BIT;const q=A.stencilBuffer?i.DEPTH_STENCIL_ATTACHMENT:i.DEPTH_ATTACHMENT,xe=n.get(A),se=v.length>1;if(se)for(let de=0;de<v.length;de++)t.bindFramebuffer(i.FRAMEBUFFER,xe.__webglMultisampledFramebuffer),i.framebufferRenderbuffer(i.FRAMEBUFFER,i.COLOR_ATTACHMENT0+de,i.RENDERBUFFER,null),t.bindFramebuffer(i.FRAMEBUFFER,xe.__webglFramebuffer),i.framebufferTexture2D(i.DRAW_FRAMEBUFFER,i.COLOR_ATTACHMENT0+de,i.TEXTURE_2D,null,0);t.bindFramebuffer(i.READ_FRAMEBUFFER,xe.__webglMultisampledFramebuffer),t.bindFramebuffer(i.DRAW_FRAMEBUFFER,xe.__webglFramebuffer);for(let de=0;de<v.length;de++){if(A.resolveDepthBuffer&&(A.depthBuffer&&(j|=i.DEPTH_BUFFER_BIT),A.stencilBuffer&&A.resolveStencilBuffer&&(j|=i.STENCIL_BUFFER_BIT)),se){i.framebufferRenderbuffer(i.READ_FRAMEBUFFER,i.COLOR_ATTACHMENT0,i.RENDERBUFFER,xe.__webglColorRenderbuffer[de]);const We=n.get(v[de]).__webglTexture;i.framebufferTexture2D(i.DRAW_FRAMEBUFFER,i.COLOR_ATTACHMENT0,i.TEXTURE_2D,We,0)}i.blitFramebuffer(0,0,F,Y,0,0,F,Y,j,i.NEAREST),l===!0&&(Se.length=0,C.length=0,Se.push(i.COLOR_ATTACHMENT0+de),A.depthBuffer&&A.resolveDepthBuffer===!1&&(Se.push(q),C.push(q),i.invalidateFramebuffer(i.DRAW_FRAMEBUFFER,C)),i.invalidateFramebuffer(i.READ_FRAMEBUFFER,Se))}if(t.bindFramebuffer(i.READ_FRAMEBUFFER,null),t.bindFramebuffer(i.DRAW_FRAMEBUFFER,null),se)for(let de=0;de<v.length;de++){t.bindFramebuffer(i.FRAMEBUFFER,xe.__webglMultisampledFramebuffer),i.framebufferRenderbuffer(i.FRAMEBUFFER,i.COLOR_ATTACHMENT0+de,i.RENDERBUFFER,xe.__webglColorRenderbuffer[de]);const We=n.get(v[de]).__webglTexture;t.bindFramebuffer(i.FRAMEBUFFER,xe.__webglFramebuffer),i.framebufferTexture2D(i.DRAW_FRAMEBUFFER,i.COLOR_ATTACHMENT0+de,i.TEXTURE_2D,We,0)}t.bindFramebuffer(i.DRAW_FRAMEBUFFER,xe.__webglMultisampledFramebuffer)}else if(A.depthBuffer&&A.resolveDepthBuffer===!1&&l){const v=A.stencilBuffer?i.DEPTH_STENCIL_ATTACHMENT:i.DEPTH_ATTACHMENT;i.invalidateFramebuffer(i.DRAW_FRAMEBUFFER,[v])}}}function Ue(A){return Math.min(r.maxSamples,A.samples)}function Ie(A){const v=n.get(A);return A.samples>0&&e.has("WEBGL_multisampled_render_to_texture")===!0&&v.__useRenderToTexture!==!1}function Te(A){const v=a.render.frame;d.get(A)!==v&&(d.set(A,v),A.update())}function ke(A,v){const F=A.colorSpace,Y=A.format,j=A.type;return A.isCompressedTexture===!0||A.isVideoTexture===!0||F!==_n&&F!==hn&&(Ke.getTransfer(F)===Qe?(Y!==Ht||j!==nn)&&console.warn("THREE.WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):console.error("THREE.WebGLTextures: Unsupported texture color space:",F)),v}function Ee(A){return typeof HTMLImageElement<"u"&&A instanceof HTMLImageElement?(c.width=A.naturalWidth||A.width,c.height=A.naturalHeight||A.height):typeof VideoFrame<"u"&&A instanceof VideoFrame?(c.width=A.displayWidth,c.height=A.displayHeight):(c.width=A.width,c.height=A.height),c}this.allocateTextureUnit=k,this.resetTextureUnits=E,this.setTexture2D=X,this.setTexture2DArray=J,this.setTexture3D=H,this.setTextureCube=Q,this.rebindTextures=ee,this.setupRenderTarget=be,this.updateRenderTargetMipmap=Ne,this.updateMultisampleRenderTarget=it,this.setupDepthRenderbuffer=ge,this.setupFrameBufferTexture=B,this.useMultisampledRTT=Ie}function up(i,e){function t(n,r=hn){let s;const a=Ke.getTransfer(r);if(n===nn)return i.UNSIGNED_BYTE;if(n===Ys)return i.UNSIGNED_SHORT_4_4_4_4;if(n===Ks)return i.UNSIGNED_SHORT_5_5_5_1;if(n===yo)return i.UNSIGNED_INT_5_9_9_9_REV;if(n===So)return i.BYTE;if(n===Eo)return i.SHORT;if(n===vi)return i.UNSIGNED_SHORT;if(n===qs)return i.INT;if(n===Pn)return i.UNSIGNED_INT;if(n===en)return i.FLOAT;if(n===xi)return i.HALF_FLOAT;if(n===To)return i.ALPHA;if(n===Ao)return i.RGB;if(n===Ht)return i.RGBA;if(n===bo)return i.LUMINANCE;if(n===wo)return i.LUMINANCE_ALPHA;if(n===$n)return i.DEPTH_COMPONENT;if(n===ti)return i.DEPTH_STENCIL;if(n===Ro)return i.RED;if(n===$s)return i.RED_INTEGER;if(n===Co)return i.RG;if(n===Zs)return i.RG_INTEGER;if(n===js)return i.RGBA_INTEGER;if(n===ji||n===Ji||n===Qi||n===er)if(a===Qe)if(s=e.get("WEBGL_compressed_texture_s3tc_srgb"),s!==null){if(n===ji)return s.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(n===Ji)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(n===Qi)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(n===er)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(s=e.get("WEBGL_compressed_texture_s3tc"),s!==null){if(n===ji)return s.COMPRESSED_RGB_S3TC_DXT1_EXT;if(n===Ji)return s.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(n===Qi)return s.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(n===er)return s.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(n===gs||n===vs||n===xs||n===Ms)if(s=e.get("WEBGL_compressed_texture_pvrtc"),s!==null){if(n===gs)return s.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(n===vs)return s.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(n===xs)return s.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(n===Ms)return s.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(n===Ss||n===Es||n===ys)if(s=e.get("WEBGL_compressed_texture_etc"),s!==null){if(n===Ss||n===Es)return a===Qe?s.COMPRESSED_SRGB8_ETC2:s.COMPRESSED_RGB8_ETC2;if(n===ys)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:s.COMPRESSED_RGBA8_ETC2_EAC}else return null;if(n===Ts||n===As||n===bs||n===ws||n===Rs||n===Cs||n===Ps||n===Ls||n===Ds||n===Us||n===Is||n===Ns||n===Fs||n===Os)if(s=e.get("WEBGL_compressed_texture_astc"),s!==null){if(n===Ts)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:s.COMPRESSED_RGBA_ASTC_4x4_KHR;if(n===As)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:s.COMPRESSED_RGBA_ASTC_5x4_KHR;if(n===bs)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:s.COMPRESSED_RGBA_ASTC_5x5_KHR;if(n===ws)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:s.COMPRESSED_RGBA_ASTC_6x5_KHR;if(n===Rs)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:s.COMPRESSED_RGBA_ASTC_6x6_KHR;if(n===Cs)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:s.COMPRESSED_RGBA_ASTC_8x5_KHR;if(n===Ps)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:s.COMPRESSED_RGBA_ASTC_8x6_KHR;if(n===Ls)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:s.COMPRESSED_RGBA_ASTC_8x8_KHR;if(n===Ds)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:s.COMPRESSED_RGBA_ASTC_10x5_KHR;if(n===Us)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:s.COMPRESSED_RGBA_ASTC_10x6_KHR;if(n===Is)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:s.COMPRESSED_RGBA_ASTC_10x8_KHR;if(n===Ns)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:s.COMPRESSED_RGBA_ASTC_10x10_KHR;if(n===Fs)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:s.COMPRESSED_RGBA_ASTC_12x10_KHR;if(n===Os)return a===Qe?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:s.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(n===tr||n===Bs||n===zs)if(s=e.get("EXT_texture_compression_bptc"),s!==null){if(n===tr)return a===Qe?s.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:s.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(n===Bs)return s.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(n===zs)return s.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(n===Po||n===Hs||n===Gs||n===Vs)if(s=e.get("EXT_texture_compression_rgtc"),s!==null){if(n===tr)return s.COMPRESSED_RED_RGTC1_EXT;if(n===Hs)return s.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(n===Gs)return s.COMPRESSED_RED_GREEN_RGTC2_EXT;if(n===Vs)return s.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return n===ei?i.UNSIGNED_INT_24_8:i[n]!==void 0?i[n]:null}return{convert:t}}class hp extends bt{constructor(e=[]){super(),this.isArrayCamera=!0,this.cameras=e}}class gi extends ht{constructor(){super(),this.isGroup=!0,this.type="Group"}}const dp={type:"move"};class es{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new gi,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new gi,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new D,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new D),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new gi,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new D,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new D),this._grip}dispatchEvent(e){return this._targetRay!==null&&this._targetRay.dispatchEvent(e),this._grip!==null&&this._grip.dispatchEvent(e),this._hand!==null&&this._hand.dispatchEvent(e),this}connect(e){if(e&&e.hand){const t=this._hand;if(t)for(const n of e.hand.values())this._getHandJoint(t,n)}return this.dispatchEvent({type:"connected",data:e}),this}disconnect(e){return this.dispatchEvent({type:"disconnected",data:e}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(e,t,n){let r=null,s=null,a=null;const o=this._targetRay,l=this._grip,c=this._hand;if(e&&t.session.visibilityState!=="visible-blurred"){if(c&&e.hand){a=!0;for(const M of e.hand.values()){const h=t.getJointPose(M,n),u=this._getHandJoint(c,M);h!==null&&(u.matrix.fromArray(h.transform.matrix),u.matrix.decompose(u.position,u.rotation,u.scale),u.matrixWorldNeedsUpdate=!0,u.jointRadius=h.radius),u.visible=h!==null}const d=c.joints["index-finger-tip"],p=c.joints["thumb-tip"],f=d.position.distanceTo(p.position),m=.02,_=.005;c.inputState.pinching&&f>m+_?(c.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:e.handedness,target:this})):!c.inputState.pinching&&f<=m-_&&(c.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:e.handedness,target:this}))}else l!==null&&e.gripSpace&&(s=t.getPose(e.gripSpace,n),s!==null&&(l.matrix.fromArray(s.transform.matrix),l.matrix.decompose(l.position,l.rotation,l.scale),l.matrixWorldNeedsUpdate=!0,s.linearVelocity?(l.hasLinearVelocity=!0,l.linearVelocity.copy(s.linearVelocity)):l.hasLinearVelocity=!1,s.angularVelocity?(l.hasAngularVelocity=!0,l.angularVelocity.copy(s.angularVelocity)):l.hasAngularVelocity=!1));o!==null&&(r=t.getPose(e.targetRaySpace,n),r===null&&s!==null&&(r=s),r!==null&&(o.matrix.fromArray(r.transform.matrix),o.matrix.decompose(o.position,o.rotation,o.scale),o.matrixWorldNeedsUpdate=!0,r.linearVelocity?(o.hasLinearVelocity=!0,o.linearVelocity.copy(r.linearVelocity)):o.hasLinearVelocity=!1,r.angularVelocity?(o.hasAngularVelocity=!0,o.angularVelocity.copy(r.angularVelocity)):o.hasAngularVelocity=!1,this.dispatchEvent(dp)))}return o!==null&&(o.visible=r!==null),l!==null&&(l.visible=s!==null),c!==null&&(c.visible=a!==null),this}_getHandJoint(e,t){if(e.joints[t.jointName]===void 0){const n=new gi;n.matrixAutoUpdate=!1,n.visible=!1,e.joints[t.jointName]=n,e.add(n)}return e.joints[t.jointName]}}const fp=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,pp=`
uniform sampler2DArray depthColor;
uniform float depthWidth;
uniform float depthHeight;

void main() {

	vec2 coord = vec2( gl_FragCoord.x / depthWidth, gl_FragCoord.y / depthHeight );

	if ( coord.x >= 1.0 ) {

		gl_FragDepth = texture( depthColor, vec3( coord.x - 1.0, coord.y, 1 ) ).r;

	} else {

		gl_FragDepth = texture( depthColor, vec3( coord.x, coord.y, 0 ) ).r;

	}

}`;class mp{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(e,t,n){if(this.texture===null){const r=new St,s=e.properties.get(r);s.__webglTexture=t.texture,(t.depthNear!=n.depthNear||t.depthFar!=n.depthFar)&&(this.depthNear=t.depthNear,this.depthFar=t.depthFar),this.texture=r}}getMesh(e){if(this.texture!==null&&this.mesh===null){const t=e.cameras[0].viewport,n=new mn({vertexShader:fp,fragmentShader:pp,uniforms:{depthColor:{value:this.texture},depthWidth:{value:t.z},depthHeight:{value:t.w}}});this.mesh=new wt(new _r(20,20),n)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class _p extends ii{constructor(e,t){super();const n=this;let r=null,s=1,a=null,o="local-floor",l=1,c=null,d=null,p=null,f=null,m=null,_=null;const M=new mp,h=t.getContextAttributes();let u=null,y=null;const S=[],T=[],N=new ze;let R=null;const w=new bt;w.layers.enable(1),w.viewport=new Ze;const I=new bt;I.layers.enable(2),I.viewport=new Ze;const K=[w,I],g=new hp;g.layers.enable(1),g.layers.enable(2);let E=null,k=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(b){let B=S[b];return B===void 0&&(B=new es,S[b]=B),B.getTargetRaySpace()},this.getControllerGrip=function(b){let B=S[b];return B===void 0&&(B=new es,S[b]=B),B.getGripSpace()},this.getHand=function(b){let B=S[b];return B===void 0&&(B=new es,S[b]=B),B.getHandSpace()};function G(b){const B=T.indexOf(b.inputSource);if(B===-1)return;const ie=S[B];ie!==void 0&&(ie.update(b.inputSource,b.frame,c||a),ie.dispatchEvent({type:b.type,data:b.inputSource}))}function X(){r.removeEventListener("select",G),r.removeEventListener("selectstart",G),r.removeEventListener("selectend",G),r.removeEventListener("squeeze",G),r.removeEventListener("squeezestart",G),r.removeEventListener("squeezeend",G),r.removeEventListener("end",X),r.removeEventListener("inputsourceschange",J);for(let b=0;b<S.length;b++){const B=T[b];B!==null&&(T[b]=null,S[b].disconnect(B))}E=null,k=null,M.reset(),e.setRenderTarget(u),m=null,f=null,p=null,r=null,y=null,Ve.stop(),n.isPresenting=!1,e.setPixelRatio(R),e.setSize(N.width,N.height,!1),n.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(b){s=b,n.isPresenting===!0&&console.warn("THREE.WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(b){o=b,n.isPresenting===!0&&console.warn("THREE.WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return c||a},this.setReferenceSpace=function(b){c=b},this.getBaseLayer=function(){return f!==null?f:m},this.getBinding=function(){return p},this.getFrame=function(){return _},this.getSession=function(){return r},this.setSession=async function(b){if(r=b,r!==null){if(u=e.getRenderTarget(),r.addEventListener("select",G),r.addEventListener("selectstart",G),r.addEventListener("selectend",G),r.addEventListener("squeeze",G),r.addEventListener("squeezestart",G),r.addEventListener("squeezeend",G),r.addEventListener("end",X),r.addEventListener("inputsourceschange",J),h.xrCompatible!==!0&&await t.makeXRCompatible(),R=e.getPixelRatio(),e.getSize(N),r.renderState.layers===void 0){const B={antialias:h.antialias,alpha:!0,depth:h.depth,stencil:h.stencil,framebufferScaleFactor:s};m=new XRWebGLLayer(r,t,B),r.updateRenderState({baseLayer:m}),e.setPixelRatio(1),e.setSize(m.framebufferWidth,m.framebufferHeight,!1),y=new Ln(m.framebufferWidth,m.framebufferHeight,{format:Ht,type:nn,colorSpace:e.outputColorSpace,stencilBuffer:h.stencil})}else{let B=null,ie=null,Z=null;h.depth&&(Z=h.stencil?t.DEPTH24_STENCIL8:t.DEPTH_COMPONENT24,B=h.stencil?ti:$n,ie=h.stencil?ei:Pn);const ge={colorFormat:t.RGBA8,depthFormat:Z,scaleFactor:s};p=new XRWebGLBinding(r,t),f=p.createProjectionLayer(ge),r.updateRenderState({layers:[f]}),e.setPixelRatio(1),e.setSize(f.textureWidth,f.textureHeight,!1),y=new Ln(f.textureWidth,f.textureHeight,{format:Ht,type:nn,depthTexture:new qo(f.textureWidth,f.textureHeight,ie,void 0,void 0,void 0,void 0,void 0,void 0,B),stencilBuffer:h.stencil,colorSpace:e.outputColorSpace,samples:h.antialias?4:0,resolveDepthBuffer:f.ignoreDepthValues===!1})}y.isXRRenderTarget=!0,this.setFoveation(l),c=null,a=await r.requestReferenceSpace(o),Ve.setContext(r),Ve.start(),n.isPresenting=!0,n.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(r!==null)return r.environmentBlendMode},this.getDepthTexture=function(){return M.getDepthTexture()};function J(b){for(let B=0;B<b.removed.length;B++){const ie=b.removed[B],Z=T.indexOf(ie);Z>=0&&(T[Z]=null,S[Z].disconnect(ie))}for(let B=0;B<b.added.length;B++){const ie=b.added[B];let Z=T.indexOf(ie);if(Z===-1){for(let ee=0;ee<S.length;ee++)if(ee>=T.length){T.push(ie),Z=ee;break}else if(T[ee]===null){T[ee]=ie,Z=ee;break}if(Z===-1)break}const ge=S[Z];ge&&ge.connect(ie)}}const H=new D,Q=new D;function W(b,B,ie){H.setFromMatrixPosition(B.matrixWorld),Q.setFromMatrixPosition(ie.matrixWorld);const Z=H.distanceTo(Q),ge=B.projectionMatrix.elements,ee=ie.projectionMatrix.elements,be=ge[14]/(ge[10]-1),Ne=ge[14]/(ge[10]+1),Se=(ge[9]+1)/ge[5],C=(ge[9]-1)/ge[5],it=(ge[8]-1)/ge[0],Ue=(ee[8]+1)/ee[0],Ie=be*it,Te=be*Ue,ke=Z/(-it+Ue),Ee=ke*-it;if(B.matrixWorld.decompose(b.position,b.quaternion,b.scale),b.translateX(Ee),b.translateZ(ke),b.matrixWorld.compose(b.position,b.quaternion,b.scale),b.matrixWorldInverse.copy(b.matrixWorld).invert(),ge[10]===-1)b.projectionMatrix.copy(B.projectionMatrix),b.projectionMatrixInverse.copy(B.projectionMatrixInverse);else{const A=be+ke,v=Ne+ke,F=Ie-Ee,Y=Te+(Z-Ee),j=Se*Ne/v*A,q=C*Ne/v*A;b.projectionMatrix.makePerspective(F,Y,j,q,A,v),b.projectionMatrixInverse.copy(b.projectionMatrix).invert()}}function le(b,B){B===null?b.matrixWorld.copy(b.matrix):b.matrixWorld.multiplyMatrices(B.matrixWorld,b.matrix),b.matrixWorldInverse.copy(b.matrixWorld).invert()}this.updateCamera=function(b){if(r===null)return;let B=b.near,ie=b.far;M.texture!==null&&(M.depthNear>0&&(B=M.depthNear),M.depthFar>0&&(ie=M.depthFar)),g.near=I.near=w.near=B,g.far=I.far=w.far=ie,(E!==g.near||k!==g.far)&&(r.updateRenderState({depthNear:g.near,depthFar:g.far}),E=g.near,k=g.far);const Z=b.parent,ge=g.cameras;le(g,Z);for(let ee=0;ee<ge.length;ee++)le(ge[ee],Z);ge.length===2?W(g,w,I):g.projectionMatrix.copy(w.projectionMatrix),ce(b,g,Z)};function ce(b,B,ie){ie===null?b.matrix.copy(B.matrixWorld):(b.matrix.copy(ie.matrixWorld),b.matrix.invert(),b.matrix.multiply(B.matrixWorld)),b.matrix.decompose(b.position,b.quaternion,b.scale),b.updateMatrixWorld(!0),b.projectionMatrix.copy(B.projectionMatrix),b.projectionMatrixInverse.copy(B.projectionMatrixInverse),b.isPerspectiveCamera&&(b.fov=ks*2*Math.atan(1/b.projectionMatrix.elements[5]),b.zoom=1)}this.getCamera=function(){return g},this.getFoveation=function(){if(!(f===null&&m===null))return l},this.setFoveation=function(b){l=b,f!==null&&(f.fixedFoveation=b),m!==null&&m.fixedFoveation!==void 0&&(m.fixedFoveation=b)},this.hasDepthSensing=function(){return M.texture!==null},this.getDepthSensingMesh=function(){return M.getMesh(g)};let _e=null;function He(b,B){if(d=B.getViewerPose(c||a),_=B,d!==null){const ie=d.views;m!==null&&(e.setRenderTargetFramebuffer(y,m.framebuffer),e.setRenderTarget(y));let Z=!1;ie.length!==g.cameras.length&&(g.cameras.length=0,Z=!0);for(let ee=0;ee<ie.length;ee++){const be=ie[ee];let Ne=null;if(m!==null)Ne=m.getViewport(be);else{const C=p.getViewSubImage(f,be);Ne=C.viewport,ee===0&&(e.setRenderTargetTextures(y,C.colorTexture,f.ignoreDepthValues?void 0:C.depthStencilTexture),e.setRenderTarget(y))}let Se=K[ee];Se===void 0&&(Se=new bt,Se.layers.enable(ee),Se.viewport=new Ze,K[ee]=Se),Se.matrix.fromArray(be.transform.matrix),Se.matrix.decompose(Se.position,Se.quaternion,Se.scale),Se.projectionMatrix.fromArray(be.projectionMatrix),Se.projectionMatrixInverse.copy(Se.projectionMatrix).invert(),Se.viewport.set(Ne.x,Ne.y,Ne.width,Ne.height),ee===0&&(g.matrix.copy(Se.matrix),g.matrix.decompose(g.position,g.quaternion,g.scale)),Z===!0&&g.cameras.push(Se)}const ge=r.enabledFeatures;if(ge&&ge.includes("depth-sensing")){const ee=p.getDepthInformation(ie[0]);ee&&ee.isValid&&ee.texture&&M.init(e,ee,r.renderState)}}for(let ie=0;ie<S.length;ie++){const Z=T[ie],ge=S[ie];Z!==null&&ge!==void 0&&ge.update(Z,B,c||a)}_e&&_e(b,B),B.detectedPlanes&&n.dispatchEvent({type:"planesdetected",data:B}),_=null}const Ve=new Wo;Ve.setAnimationLoop(He),this.setAnimationLoop=function(b){_e=b},this.dispose=function(){}}}const yn=new Xt,gp=new et;function vp(i,e){function t(h,u){h.matrixAutoUpdate===!0&&h.updateMatrix(),u.value.copy(h.matrix)}function n(h,u){u.color.getRGB(h.fogColor.value,Go(i)),u.isFog?(h.fogNear.value=u.near,h.fogFar.value=u.far):u.isFogExp2&&(h.fogDensity.value=u.density)}function r(h,u,y,S,T){u.isMeshBasicMaterial||u.isMeshLambertMaterial?s(h,u):u.isMeshToonMaterial?(s(h,u),p(h,u)):u.isMeshPhongMaterial?(s(h,u),d(h,u)):u.isMeshStandardMaterial?(s(h,u),f(h,u),u.isMeshPhysicalMaterial&&m(h,u,T)):u.isMeshMatcapMaterial?(s(h,u),_(h,u)):u.isMeshDepthMaterial?s(h,u):u.isMeshDistanceMaterial?(s(h,u),M(h,u)):u.isMeshNormalMaterial?s(h,u):u.isLineBasicMaterial?(a(h,u),u.isLineDashedMaterial&&o(h,u)):u.isPointsMaterial?l(h,u,y,S):u.isSpriteMaterial?c(h,u):u.isShadowMaterial?(h.color.value.copy(u.color),h.opacity.value=u.opacity):u.isShaderMaterial&&(u.uniformsNeedUpdate=!1)}function s(h,u){h.opacity.value=u.opacity,u.color&&h.diffuse.value.copy(u.color),u.emissive&&h.emissive.value.copy(u.emissive).multiplyScalar(u.emissiveIntensity),u.map&&(h.map.value=u.map,t(u.map,h.mapTransform)),u.alphaMap&&(h.alphaMap.value=u.alphaMap,t(u.alphaMap,h.alphaMapTransform)),u.bumpMap&&(h.bumpMap.value=u.bumpMap,t(u.bumpMap,h.bumpMapTransform),h.bumpScale.value=u.bumpScale,u.side===Mt&&(h.bumpScale.value*=-1)),u.normalMap&&(h.normalMap.value=u.normalMap,t(u.normalMap,h.normalMapTransform),h.normalScale.value.copy(u.normalScale),u.side===Mt&&h.normalScale.value.negate()),u.displacementMap&&(h.displacementMap.value=u.displacementMap,t(u.displacementMap,h.displacementMapTransform),h.displacementScale.value=u.displacementScale,h.displacementBias.value=u.displacementBias),u.emissiveMap&&(h.emissiveMap.value=u.emissiveMap,t(u.emissiveMap,h.emissiveMapTransform)),u.specularMap&&(h.specularMap.value=u.specularMap,t(u.specularMap,h.specularMapTransform)),u.alphaTest>0&&(h.alphaTest.value=u.alphaTest);const y=e.get(u),S=y.envMap,T=y.envMapRotation;S&&(h.envMap.value=S,yn.copy(T),yn.x*=-1,yn.y*=-1,yn.z*=-1,S.isCubeTexture&&S.isRenderTargetTexture===!1&&(yn.y*=-1,yn.z*=-1),h.envMapRotation.value.setFromMatrix4(gp.makeRotationFromEuler(yn)),h.flipEnvMap.value=S.isCubeTexture&&S.isRenderTargetTexture===!1?-1:1,h.reflectivity.value=u.reflectivity,h.ior.value=u.ior,h.refractionRatio.value=u.refractionRatio),u.lightMap&&(h.lightMap.value=u.lightMap,h.lightMapIntensity.value=u.lightMapIntensity,t(u.lightMap,h.lightMapTransform)),u.aoMap&&(h.aoMap.value=u.aoMap,h.aoMapIntensity.value=u.aoMapIntensity,t(u.aoMap,h.aoMapTransform))}function a(h,u){h.diffuse.value.copy(u.color),h.opacity.value=u.opacity,u.map&&(h.map.value=u.map,t(u.map,h.mapTransform))}function o(h,u){h.dashSize.value=u.dashSize,h.totalSize.value=u.dashSize+u.gapSize,h.scale.value=u.scale}function l(h,u,y,S){h.diffuse.value.copy(u.color),h.opacity.value=u.opacity,h.size.value=u.size*y,h.scale.value=S*.5,u.map&&(h.map.value=u.map,t(u.map,h.uvTransform)),u.alphaMap&&(h.alphaMap.value=u.alphaMap,t(u.alphaMap,h.alphaMapTransform)),u.alphaTest>0&&(h.alphaTest.value=u.alphaTest)}function c(h,u){h.diffuse.value.copy(u.color),h.opacity.value=u.opacity,h.rotation.value=u.rotation,u.map&&(h.map.value=u.map,t(u.map,h.mapTransform)),u.alphaMap&&(h.alphaMap.value=u.alphaMap,t(u.alphaMap,h.alphaMapTransform)),u.alphaTest>0&&(h.alphaTest.value=u.alphaTest)}function d(h,u){h.specular.value.copy(u.specular),h.shininess.value=Math.max(u.shininess,1e-4)}function p(h,u){u.gradientMap&&(h.gradientMap.value=u.gradientMap)}function f(h,u){h.metalness.value=u.metalness,u.metalnessMap&&(h.metalnessMap.value=u.metalnessMap,t(u.metalnessMap,h.metalnessMapTransform)),h.roughness.value=u.roughness,u.roughnessMap&&(h.roughnessMap.value=u.roughnessMap,t(u.roughnessMap,h.roughnessMapTransform)),u.envMap&&(h.envMapIntensity.value=u.envMapIntensity)}function m(h,u,y){h.ior.value=u.ior,u.sheen>0&&(h.sheenColor.value.copy(u.sheenColor).multiplyScalar(u.sheen),h.sheenRoughness.value=u.sheenRoughness,u.sheenColorMap&&(h.sheenColorMap.value=u.sheenColorMap,t(u.sheenColorMap,h.sheenColorMapTransform)),u.sheenRoughnessMap&&(h.sheenRoughnessMap.value=u.sheenRoughnessMap,t(u.sheenRoughnessMap,h.sheenRoughnessMapTransform))),u.clearcoat>0&&(h.clearcoat.value=u.clearcoat,h.clearcoatRoughness.value=u.clearcoatRoughness,u.clearcoatMap&&(h.clearcoatMap.value=u.clearcoatMap,t(u.clearcoatMap,h.clearcoatMapTransform)),u.clearcoatRoughnessMap&&(h.clearcoatRoughnessMap.value=u.clearcoatRoughnessMap,t(u.clearcoatRoughnessMap,h.clearcoatRoughnessMapTransform)),u.clearcoatNormalMap&&(h.clearcoatNormalMap.value=u.clearcoatNormalMap,t(u.clearcoatNormalMap,h.clearcoatNormalMapTransform),h.clearcoatNormalScale.value.copy(u.clearcoatNormalScale),u.side===Mt&&h.clearcoatNormalScale.value.negate())),u.dispersion>0&&(h.dispersion.value=u.dispersion),u.iridescence>0&&(h.iridescence.value=u.iridescence,h.iridescenceIOR.value=u.iridescenceIOR,h.iridescenceThicknessMinimum.value=u.iridescenceThicknessRange[0],h.iridescenceThicknessMaximum.value=u.iridescenceThicknessRange[1],u.iridescenceMap&&(h.iridescenceMap.value=u.iridescenceMap,t(u.iridescenceMap,h.iridescenceMapTransform)),u.iridescenceThicknessMap&&(h.iridescenceThicknessMap.value=u.iridescenceThicknessMap,t(u.iridescenceThicknessMap,h.iridescenceThicknessMapTransform))),u.transmission>0&&(h.transmission.value=u.transmission,h.transmissionSamplerMap.value=y.texture,h.transmissionSamplerSize.value.set(y.width,y.height),u.transmissionMap&&(h.transmissionMap.value=u.transmissionMap,t(u.transmissionMap,h.transmissionMapTransform)),h.thickness.value=u.thickness,u.thicknessMap&&(h.thicknessMap.value=u.thicknessMap,t(u.thicknessMap,h.thicknessMapTransform)),h.attenuationDistance.value=u.attenuationDistance,h.attenuationColor.value.copy(u.attenuationColor)),u.anisotropy>0&&(h.anisotropyVector.value.set(u.anisotropy*Math.cos(u.anisotropyRotation),u.anisotropy*Math.sin(u.anisotropyRotation)),u.anisotropyMap&&(h.anisotropyMap.value=u.anisotropyMap,t(u.anisotropyMap,h.anisotropyMapTransform))),h.specularIntensity.value=u.specularIntensity,h.specularColor.value.copy(u.specularColor),u.specularColorMap&&(h.specularColorMap.value=u.specularColorMap,t(u.specularColorMap,h.specularColorMapTransform)),u.specularIntensityMap&&(h.specularIntensityMap.value=u.specularIntensityMap,t(u.specularIntensityMap,h.specularIntensityMapTransform))}function _(h,u){u.matcap&&(h.matcap.value=u.matcap)}function M(h,u){const y=e.get(u).light;h.referencePosition.value.setFromMatrixPosition(y.matrixWorld),h.nearDistance.value=y.shadow.camera.near,h.farDistance.value=y.shadow.camera.far}return{refreshFogUniforms:n,refreshMaterialUniforms:r}}function xp(i,e,t,n){let r={},s={},a=[];const o=i.getParameter(i.MAX_UNIFORM_BUFFER_BINDINGS);function l(y,S){const T=S.program;n.uniformBlockBinding(y,T)}function c(y,S){let T=r[y.id];T===void 0&&(_(y),T=d(y),r[y.id]=T,y.addEventListener("dispose",h));const N=S.program;n.updateUBOMapping(y,N);const R=e.render.frame;s[y.id]!==R&&(f(y),s[y.id]=R)}function d(y){const S=p();y.__bindingPointIndex=S;const T=i.createBuffer(),N=y.__size,R=y.usage;return i.bindBuffer(i.UNIFORM_BUFFER,T),i.bufferData(i.UNIFORM_BUFFER,N,R),i.bindBuffer(i.UNIFORM_BUFFER,null),i.bindBufferBase(i.UNIFORM_BUFFER,S,T),T}function p(){for(let y=0;y<o;y++)if(a.indexOf(y)===-1)return a.push(y),y;return console.error("THREE.WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function f(y){const S=r[y.id],T=y.uniforms,N=y.__cache;i.bindBuffer(i.UNIFORM_BUFFER,S);for(let R=0,w=T.length;R<w;R++){const I=Array.isArray(T[R])?T[R]:[T[R]];for(let K=0,g=I.length;K<g;K++){const E=I[K];if(m(E,R,K,N)===!0){const k=E.__offset,G=Array.isArray(E.value)?E.value:[E.value];let X=0;for(let J=0;J<G.length;J++){const H=G[J],Q=M(H);typeof H=="number"||typeof H=="boolean"?(E.__data[0]=H,i.bufferSubData(i.UNIFORM_BUFFER,k+X,E.__data)):H.isMatrix3?(E.__data[0]=H.elements[0],E.__data[1]=H.elements[1],E.__data[2]=H.elements[2],E.__data[3]=0,E.__data[4]=H.elements[3],E.__data[5]=H.elements[4],E.__data[6]=H.elements[5],E.__data[7]=0,E.__data[8]=H.elements[6],E.__data[9]=H.elements[7],E.__data[10]=H.elements[8],E.__data[11]=0):(H.toArray(E.__data,X),X+=Q.storage/Float32Array.BYTES_PER_ELEMENT)}i.bufferSubData(i.UNIFORM_BUFFER,k,E.__data)}}}i.bindBuffer(i.UNIFORM_BUFFER,null)}function m(y,S,T,N){const R=y.value,w=S+"_"+T;if(N[w]===void 0)return typeof R=="number"||typeof R=="boolean"?N[w]=R:N[w]=R.clone(),!0;{const I=N[w];if(typeof R=="number"||typeof R=="boolean"){if(I!==R)return N[w]=R,!0}else if(I.equals(R)===!1)return I.copy(R),!0}return!1}function _(y){const S=y.uniforms;let T=0;const N=16;for(let w=0,I=S.length;w<I;w++){const K=Array.isArray(S[w])?S[w]:[S[w]];for(let g=0,E=K.length;g<E;g++){const k=K[g],G=Array.isArray(k.value)?k.value:[k.value];for(let X=0,J=G.length;X<J;X++){const H=G[X],Q=M(H),W=T%N,le=W%Q.boundary,ce=W+le;T+=le,ce!==0&&N-ce<Q.storage&&(T+=N-ce),k.__data=new Float32Array(Q.storage/Float32Array.BYTES_PER_ELEMENT),k.__offset=T,T+=Q.storage}}}const R=T%N;return R>0&&(T+=N-R),y.__size=T,y.__cache={},this}function M(y){const S={boundary:0,storage:0};return typeof y=="number"||typeof y=="boolean"?(S.boundary=4,S.storage=4):y.isVector2?(S.boundary=8,S.storage=8):y.isVector3||y.isColor?(S.boundary=16,S.storage=12):y.isVector4?(S.boundary=16,S.storage=16):y.isMatrix3?(S.boundary=48,S.storage=48):y.isMatrix4?(S.boundary=64,S.storage=64):y.isTexture?console.warn("THREE.WebGLRenderer: Texture samplers can not be part of an uniforms group."):console.warn("THREE.WebGLRenderer: Unsupported uniform value type.",y),S}function h(y){const S=y.target;S.removeEventListener("dispose",h);const T=a.indexOf(S.__bindingPointIndex);a.splice(T,1),i.deleteBuffer(r[S.id]),delete r[S.id],delete s[S.id]}function u(){for(const y in r)i.deleteBuffer(r[y]);a=[],r={},s={}}return{bind:l,update:c,dispose:u}}class Mp{constructor(e={}){const{canvas:t=ac(),context:n=null,depth:r=!0,stencil:s=!1,alpha:a=!1,antialias:o=!1,premultipliedAlpha:l=!0,preserveDrawingBuffer:c=!1,powerPreference:d="default",failIfMajorPerformanceCaveat:p=!1}=e;this.isWebGLRenderer=!0;let f;if(n!==null){if(typeof WebGLRenderingContext<"u"&&n instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");f=n.getContextAttributes().alpha}else f=a;const m=new Uint32Array(4),_=new Int32Array(4);let M=null,h=null;const u=[],y=[];this.domElement=t,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this._outputColorSpace=Vt,this.toneMapping=fn,this.toneMappingExposure=1;const S=this;let T=!1,N=0,R=0,w=null,I=-1,K=null;const g=new Ze,E=new Ze;let k=null;const G=new Oe(0);let X=0,J=t.width,H=t.height,Q=1,W=null,le=null;const ce=new Ze(0,0,J,H),_e=new Ze(0,0,J,H);let He=!1;const Ve=new Qs;let b=!1,B=!1;const ie=new et,Z=new et,ge=new D,ee=new Ze,be={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let Ne=!1;function Se(){return w===null?Q:1}let C=n;function it(x,L){return t.getContext(x,L)}try{const x={alpha:!0,depth:r,stencil:s,antialias:o,premultipliedAlpha:l,preserveDrawingBuffer:c,powerPreference:d,failIfMajorPerformanceCaveat:p};if("setAttribute"in t&&t.setAttribute("data-engine",`three.js r${Xs}`),t.addEventListener("webglcontextlost",$,!1),t.addEventListener("webglcontextrestored",ae,!1),t.addEventListener("webglcontextcreationerror",he,!1),C===null){const L="webgl2";if(C=it(L,x),C===null)throw it(L)?new Error("Error creating WebGL context with your selected attributes."):new Error("Error creating WebGL context.")}}catch(x){throw console.error("THREE.WebGLRenderer: "+x.message),x}let Ue,Ie,Te,ke,Ee,A,v,F,Y,j,q,xe,se,de,We,te,fe,Re,Ce,pe,Be,Pe,je,P;function ue(){Ue=new Td(C),Ue.init(),Pe=new up(C,Ue),Ie=new vd(C,Ue,e,Pe),Te=new op(C),Ie.reverseDepthBuffer&&Te.buffers.depth.setReversed(!0),ke=new wd(C),Ee=new qf,A=new cp(C,Ue,Te,Ee,Ie,Pe,ke),v=new Md(S),F=new yd(S),Y=new Ic(C),je=new _d(C,Y),j=new Ad(C,Y,ke,je),q=new Cd(C,j,Y,ke),Ce=new Rd(C,Ie,A),te=new xd(Ee),xe=new Xf(S,v,F,Ue,Ie,je,te),se=new vp(S,Ee),de=new Kf,We=new ep(Ue),Re=new md(S,v,F,Te,q,f,l),fe=new sp(S,q,Ie),P=new xp(C,ke,Ie,Te),pe=new gd(C,Ue,ke),Be=new bd(C,Ue,ke),ke.programs=xe.programs,S.capabilities=Ie,S.extensions=Ue,S.properties=Ee,S.renderLists=de,S.shadowMap=fe,S.state=Te,S.info=ke}ue();const V=new _p(S,C);this.xr=V,this.getContext=function(){return C},this.getContextAttributes=function(){return C.getContextAttributes()},this.forceContextLoss=function(){const x=Ue.get("WEBGL_lose_context");x&&x.loseContext()},this.forceContextRestore=function(){const x=Ue.get("WEBGL_lose_context");x&&x.restoreContext()},this.getPixelRatio=function(){return Q},this.setPixelRatio=function(x){x!==void 0&&(Q=x,this.setSize(J,H,!1))},this.getSize=function(x){return x.set(J,H)},this.setSize=function(x,L,O=!0){if(V.isPresenting){console.warn("THREE.WebGLRenderer: Can't change size while VR device is presenting.");return}J=x,H=L,t.width=Math.floor(x*Q),t.height=Math.floor(L*Q),O===!0&&(t.style.width=x+"px",t.style.height=L+"px"),this.setViewport(0,0,x,L)},this.getDrawingBufferSize=function(x){return x.set(J*Q,H*Q).floor()},this.setDrawingBufferSize=function(x,L,O){J=x,H=L,Q=O,t.width=Math.floor(x*O),t.height=Math.floor(L*O),this.setViewport(0,0,x,L)},this.getCurrentViewport=function(x){return x.copy(g)},this.getViewport=function(x){return x.copy(ce)},this.setViewport=function(x,L,O,z){x.isVector4?ce.set(x.x,x.y,x.z,x.w):ce.set(x,L,O,z),Te.viewport(g.copy(ce).multiplyScalar(Q).round())},this.getScissor=function(x){return x.copy(_e)},this.setScissor=function(x,L,O,z){x.isVector4?_e.set(x.x,x.y,x.z,x.w):_e.set(x,L,O,z),Te.scissor(E.copy(_e).multiplyScalar(Q).round())},this.getScissorTest=function(){return He},this.setScissorTest=function(x){Te.setScissorTest(He=x)},this.setOpaqueSort=function(x){W=x},this.setTransparentSort=function(x){le=x},this.getClearColor=function(x){return x.copy(Re.getClearColor())},this.setClearColor=function(){Re.setClearColor.apply(Re,arguments)},this.getClearAlpha=function(){return Re.getClearAlpha()},this.setClearAlpha=function(){Re.setClearAlpha.apply(Re,arguments)},this.clear=function(x=!0,L=!0,O=!0){let z=0;if(x){let U=!1;if(w!==null){const ne=w.texture.format;U=ne===js||ne===Zs||ne===$s}if(U){const ne=w.texture.type,oe=ne===nn||ne===Pn||ne===vi||ne===ei||ne===Ys||ne===Ks,me=Re.getClearColor(),ve=Re.getClearAlpha(),Ae=me.r,we=me.g,Me=me.b;oe?(m[0]=Ae,m[1]=we,m[2]=Me,m[3]=ve,C.clearBufferuiv(C.COLOR,0,m)):(_[0]=Ae,_[1]=we,_[2]=Me,_[3]=ve,C.clearBufferiv(C.COLOR,0,_))}else z|=C.COLOR_BUFFER_BIT}L&&(z|=C.DEPTH_BUFFER_BIT,C.clearDepth(this.capabilities.reverseDepthBuffer?0:1)),O&&(z|=C.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),C.clear(z)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.dispose=function(){t.removeEventListener("webglcontextlost",$,!1),t.removeEventListener("webglcontextrestored",ae,!1),t.removeEventListener("webglcontextcreationerror",he,!1),de.dispose(),We.dispose(),Ee.dispose(),v.dispose(),F.dispose(),q.dispose(),je.dispose(),P.dispose(),xe.dispose(),V.dispose(),V.removeEventListener("sessionstart",oa),V.removeEventListener("sessionend",la),gn.stop()};function $(x){x.preventDefault(),console.log("THREE.WebGLRenderer: Context Lost."),T=!0}function ae(){console.log("THREE.WebGLRenderer: Context Restored."),T=!1;const x=ke.autoReset,L=fe.enabled,O=fe.autoUpdate,z=fe.needsUpdate,U=fe.type;ue(),ke.autoReset=x,fe.enabled=L,fe.autoUpdate=O,fe.needsUpdate=z,fe.type=U}function he(x){console.error("THREE.WebGLRenderer: A WebGL context could not be created. Reason: ",x.statusMessage)}function Ge(x){const L=x.target;L.removeEventListener("dispose",Ge),rt(L)}function rt(x){mt(x),Ee.remove(x)}function mt(x){const L=Ee.get(x).programs;L!==void 0&&(L.forEach(function(O){xe.releaseProgram(O)}),x.isShaderMaterial&&xe.releaseShaderCache(x))}this.renderBufferDirect=function(x,L,O,z,U,ne){L===null&&(L=be);const oe=U.isMesh&&U.matrixWorld.determinant()<0,me=Qo(x,L,O,z,U);Te.setMaterial(z,oe);let ve=O.index,Ae=1;if(z.wireframe===!0){if(ve=j.getWireframeAttribute(O),ve===void 0)return;Ae=2}const we=O.drawRange,Me=O.attributes.position;let $e=we.start*Ae,Je=(we.start+we.count)*Ae;ne!==null&&($e=Math.max($e,ne.start*Ae),Je=Math.min(Je,(ne.start+ne.count)*Ae)),ve!==null?($e=Math.max($e,0),Je=Math.min(Je,ve.count)):Me!=null&&($e=Math.max($e,0),Je=Math.min(Je,Me.count));const tt=Je-$e;if(tt<0||tt===1/0)return;je.setup(U,z,me,O,ve);let Et,qe=pe;if(ve!==null&&(Et=Y.get(ve),qe=Be,qe.setIndex(Et)),U.isMesh)z.wireframe===!0?(Te.setLineWidth(z.wireframeLinewidth*Se()),qe.setMode(C.LINES)):qe.setMode(C.TRIANGLES);else if(U.isLine){let ye=z.linewidth;ye===void 0&&(ye=1),Te.setLineWidth(ye*Se()),U.isLineSegments?qe.setMode(C.LINES):U.isLineLoop?qe.setMode(C.LINE_LOOP):qe.setMode(C.LINE_STRIP)}else U.isPoints?qe.setMode(C.POINTS):U.isSprite&&qe.setMode(C.TRIANGLES);if(U.isBatchedMesh)if(U._multiDrawInstances!==null)qe.renderMultiDrawInstances(U._multiDrawStarts,U._multiDrawCounts,U._multiDrawCount,U._multiDrawInstances);else if(Ue.get("WEBGL_multi_draw"))qe.renderMultiDraw(U._multiDrawStarts,U._multiDrawCounts,U._multiDrawCount);else{const ye=U._multiDrawStarts,ut=U._multiDrawCounts,Ye=U._multiDrawCount,Dt=ve?Y.get(ve).bytesPerElement:1,Dn=Ee.get(z).currentProgram.getUniforms();for(let yt=0;yt<Ye;yt++)Dn.setValue(C,"_gl_DrawID",yt),qe.render(ye[yt]/Dt,ut[yt])}else if(U.isInstancedMesh)qe.renderInstances($e,tt,U.count);else if(O.isInstancedBufferGeometry){const ye=O._maxInstanceCount!==void 0?O._maxInstanceCount:1/0,ut=Math.min(O.instanceCount,ye);qe.renderInstances($e,tt,ut)}else qe.render($e,tt)};function Xe(x,L,O){x.transparent===!0&&x.side===Qt&&x.forceSinglePass===!1?(x.side=Mt,x.needsUpdate=!0,Ai(x,L,O),x.side=pn,x.needsUpdate=!0,Ai(x,L,O),x.side=Qt):Ai(x,L,O)}this.compile=function(x,L,O=null){O===null&&(O=x),h=We.get(O),h.init(L),y.push(h),O.traverseVisible(function(U){U.isLight&&U.layers.test(L.layers)&&(h.pushLight(U),U.castShadow&&h.pushShadow(U))}),x!==O&&x.traverseVisible(function(U){U.isLight&&U.layers.test(L.layers)&&(h.pushLight(U),U.castShadow&&h.pushShadow(U))}),h.setupLights();const z=new Set;return x.traverse(function(U){if(!(U.isMesh||U.isPoints||U.isLine||U.isSprite))return;const ne=U.material;if(ne)if(Array.isArray(ne))for(let oe=0;oe<ne.length;oe++){const me=ne[oe];Xe(me,O,U),z.add(me)}else Xe(ne,O,U),z.add(ne)}),y.pop(),h=null,z},this.compileAsync=function(x,L,O=null){const z=this.compile(x,L,O);return new Promise(U=>{function ne(){if(z.forEach(function(oe){Ee.get(oe).currentProgram.isReady()&&z.delete(oe)}),z.size===0){U(x);return}setTimeout(ne,10)}Ue.get("KHR_parallel_shader_compile")!==null?ne():setTimeout(ne,10)})};let _t=null;function qt(x){_t&&_t(x)}function oa(){gn.stop()}function la(){gn.start()}const gn=new Wo;gn.setAnimationLoop(qt),typeof self<"u"&&gn.setContext(self),this.setAnimationLoop=function(x){_t=x,V.setAnimationLoop(x),x===null?gn.stop():gn.start()},V.addEventListener("sessionstart",oa),V.addEventListener("sessionend",la),this.render=function(x,L){if(L!==void 0&&L.isCamera!==!0){console.error("THREE.WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(T===!0)return;if(x.matrixWorldAutoUpdate===!0&&x.updateMatrixWorld(),L.parent===null&&L.matrixWorldAutoUpdate===!0&&L.updateMatrixWorld(),V.enabled===!0&&V.isPresenting===!0&&(V.cameraAutoUpdate===!0&&V.updateCamera(L),L=V.getCamera()),x.isScene===!0&&x.onBeforeRender(S,x,L,w),h=We.get(x,y.length),h.init(L),y.push(h),Z.multiplyMatrices(L.projectionMatrix,L.matrixWorldInverse),Ve.setFromProjectionMatrix(Z),B=this.localClippingEnabled,b=te.init(this.clippingPlanes,B),M=de.get(x,u.length),M.init(),u.push(M),V.enabled===!0&&V.isPresenting===!0){const ne=S.xr.getDepthSensingMesh();ne!==null&&vr(ne,L,-1/0,S.sortObjects)}vr(x,L,0,S.sortObjects),M.finish(),S.sortObjects===!0&&M.sort(W,le),Ne=V.enabled===!1||V.isPresenting===!1||V.hasDepthSensing()===!1,Ne&&Re.addToRenderList(M,x),this.info.render.frame++,b===!0&&te.beginShadows();const O=h.state.shadowsArray;fe.render(O,x,L),b===!0&&te.endShadows(),this.info.autoReset===!0&&this.info.reset();const z=M.opaque,U=M.transmissive;if(h.setupLights(),L.isArrayCamera){const ne=L.cameras;if(U.length>0)for(let oe=0,me=ne.length;oe<me;oe++){const ve=ne[oe];ua(z,U,x,ve)}Ne&&Re.render(x);for(let oe=0,me=ne.length;oe<me;oe++){const ve=ne[oe];ca(M,x,ve,ve.viewport)}}else U.length>0&&ua(z,U,x,L),Ne&&Re.render(x),ca(M,x,L);w!==null&&(A.updateMultisampleRenderTarget(w),A.updateRenderTargetMipmap(w)),x.isScene===!0&&x.onAfterRender(S,x,L),je.resetDefaultState(),I=-1,K=null,y.pop(),y.length>0?(h=y[y.length-1],b===!0&&te.setGlobalState(S.clippingPlanes,h.state.camera)):h=null,u.pop(),u.length>0?M=u[u.length-1]:M=null};function vr(x,L,O,z){if(x.visible===!1)return;if(x.layers.test(L.layers)){if(x.isGroup)O=x.renderOrder;else if(x.isLOD)x.autoUpdate===!0&&x.update(L);else if(x.isLight)h.pushLight(x),x.castShadow&&h.pushShadow(x);else if(x.isSprite){if(!x.frustumCulled||Ve.intersectsSprite(x)){z&&ee.setFromMatrixPosition(x.matrixWorld).applyMatrix4(Z);const oe=q.update(x),me=x.material;me.visible&&M.push(x,oe,me,O,ee.z,null)}}else if((x.isMesh||x.isLine||x.isPoints)&&(!x.frustumCulled||Ve.intersectsObject(x))){const oe=q.update(x),me=x.material;if(z&&(x.boundingSphere!==void 0?(x.boundingSphere===null&&x.computeBoundingSphere(),ee.copy(x.boundingSphere.center)):(oe.boundingSphere===null&&oe.computeBoundingSphere(),ee.copy(oe.boundingSphere.center)),ee.applyMatrix4(x.matrixWorld).applyMatrix4(Z)),Array.isArray(me)){const ve=oe.groups;for(let Ae=0,we=ve.length;Ae<we;Ae++){const Me=ve[Ae],$e=me[Me.materialIndex];$e&&$e.visible&&M.push(x,oe,$e,O,ee.z,Me)}}else me.visible&&M.push(x,oe,me,O,ee.z,null)}}const ne=x.children;for(let oe=0,me=ne.length;oe<me;oe++)vr(ne[oe],L,O,z)}function ca(x,L,O,z){const U=x.opaque,ne=x.transmissive,oe=x.transparent;h.setupLightsView(O),b===!0&&te.setGlobalState(S.clippingPlanes,O),z&&Te.viewport(g.copy(z)),U.length>0&&Ti(U,L,O),ne.length>0&&Ti(ne,L,O),oe.length>0&&Ti(oe,L,O),Te.buffers.depth.setTest(!0),Te.buffers.depth.setMask(!0),Te.buffers.color.setMask(!0),Te.setPolygonOffset(!1)}function ua(x,L,O,z){if((O.isScene===!0?O.overrideMaterial:null)!==null)return;h.state.transmissionRenderTarget[z.id]===void 0&&(h.state.transmissionRenderTarget[z.id]=new Ln(1,1,{generateMipmaps:!0,type:Ue.has("EXT_color_buffer_half_float")||Ue.has("EXT_color_buffer_float")?xi:nn,minFilter:Cn,samples:4,stencilBuffer:s,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:Ke.workingColorSpace}));const ne=h.state.transmissionRenderTarget[z.id],oe=z.viewport||g;ne.setSize(oe.z,oe.w);const me=S.getRenderTarget();S.setRenderTarget(ne),S.getClearColor(G),X=S.getClearAlpha(),X<1&&S.setClearColor(16777215,.5),S.clear(),Ne&&Re.render(O);const ve=S.toneMapping;S.toneMapping=fn;const Ae=z.viewport;if(z.viewport!==void 0&&(z.viewport=void 0),h.setupLightsView(z),b===!0&&te.setGlobalState(S.clippingPlanes,z),Ti(x,O,z),A.updateMultisampleRenderTarget(ne),A.updateRenderTargetMipmap(ne),Ue.has("WEBGL_multisampled_render_to_texture")===!1){let we=!1;for(let Me=0,$e=L.length;Me<$e;Me++){const Je=L[Me],tt=Je.object,Et=Je.geometry,qe=Je.material,ye=Je.group;if(qe.side===Qt&&tt.layers.test(z.layers)){const ut=qe.side;qe.side=Mt,qe.needsUpdate=!0,ha(tt,O,z,Et,qe,ye),qe.side=ut,qe.needsUpdate=!0,we=!0}}we===!0&&(A.updateMultisampleRenderTarget(ne),A.updateRenderTargetMipmap(ne))}S.setRenderTarget(me),S.setClearColor(G,X),Ae!==void 0&&(z.viewport=Ae),S.toneMapping=ve}function Ti(x,L,O){const z=L.isScene===!0?L.overrideMaterial:null;for(let U=0,ne=x.length;U<ne;U++){const oe=x[U],me=oe.object,ve=oe.geometry,Ae=z===null?oe.material:z,we=oe.group;me.layers.test(O.layers)&&ha(me,L,O,ve,Ae,we)}}function ha(x,L,O,z,U,ne){x.onBeforeRender(S,L,O,z,U,ne),x.modelViewMatrix.multiplyMatrices(O.matrixWorldInverse,x.matrixWorld),x.normalMatrix.getNormalMatrix(x.modelViewMatrix),U.onBeforeRender(S,L,O,z,x,ne),U.transparent===!0&&U.side===Qt&&U.forceSinglePass===!1?(U.side=Mt,U.needsUpdate=!0,S.renderBufferDirect(O,L,z,U,x,ne),U.side=pn,U.needsUpdate=!0,S.renderBufferDirect(O,L,z,U,x,ne),U.side=Qt):S.renderBufferDirect(O,L,z,U,x,ne),x.onAfterRender(S,L,O,z,U,ne)}function Ai(x,L,O){L.isScene!==!0&&(L=be);const z=Ee.get(x),U=h.state.lights,ne=h.state.shadowsArray,oe=U.state.version,me=xe.getParameters(x,U.state,ne,L,O),ve=xe.getProgramCacheKey(me);let Ae=z.programs;z.environment=x.isMeshStandardMaterial?L.environment:null,z.fog=L.fog,z.envMap=(x.isMeshStandardMaterial?F:v).get(x.envMap||z.environment),z.envMapRotation=z.environment!==null&&x.envMap===null?L.environmentRotation:x.envMapRotation,Ae===void 0&&(x.addEventListener("dispose",Ge),Ae=new Map,z.programs=Ae);let we=Ae.get(ve);if(we!==void 0){if(z.currentProgram===we&&z.lightsStateVersion===oe)return fa(x,me),we}else me.uniforms=xe.getUniforms(x),x.onBeforeCompile(me,S),we=xe.acquireProgram(me,ve),Ae.set(ve,we),z.uniforms=me.uniforms;const Me=z.uniforms;return(!x.isShaderMaterial&&!x.isRawShaderMaterial||x.clipping===!0)&&(Me.clippingPlanes=te.uniform),fa(x,me),z.needsLights=tl(x),z.lightsStateVersion=oe,z.needsLights&&(Me.ambientLightColor.value=U.state.ambient,Me.lightProbe.value=U.state.probe,Me.directionalLights.value=U.state.directional,Me.directionalLightShadows.value=U.state.directionalShadow,Me.spotLights.value=U.state.spot,Me.spotLightShadows.value=U.state.spotShadow,Me.rectAreaLights.value=U.state.rectArea,Me.ltc_1.value=U.state.rectAreaLTC1,Me.ltc_2.value=U.state.rectAreaLTC2,Me.pointLights.value=U.state.point,Me.pointLightShadows.value=U.state.pointShadow,Me.hemisphereLights.value=U.state.hemi,Me.directionalShadowMap.value=U.state.directionalShadowMap,Me.directionalShadowMatrix.value=U.state.directionalShadowMatrix,Me.spotShadowMap.value=U.state.spotShadowMap,Me.spotLightMatrix.value=U.state.spotLightMatrix,Me.spotLightMap.value=U.state.spotLightMap,Me.pointShadowMap.value=U.state.pointShadowMap,Me.pointShadowMatrix.value=U.state.pointShadowMatrix),z.currentProgram=we,z.uniformsList=null,we}function da(x){if(x.uniformsList===null){const L=x.currentProgram.getUniforms();x.uniformsList=ir.seqWithValue(L.seq,x.uniforms)}return x.uniformsList}function fa(x,L){const O=Ee.get(x);O.outputColorSpace=L.outputColorSpace,O.batching=L.batching,O.batchingColor=L.batchingColor,O.instancing=L.instancing,O.instancingColor=L.instancingColor,O.instancingMorph=L.instancingMorph,O.skinning=L.skinning,O.morphTargets=L.morphTargets,O.morphNormals=L.morphNormals,O.morphColors=L.morphColors,O.morphTargetsCount=L.morphTargetsCount,O.numClippingPlanes=L.numClippingPlanes,O.numIntersection=L.numClipIntersection,O.vertexAlphas=L.vertexAlphas,O.vertexTangents=L.vertexTangents,O.toneMapping=L.toneMapping}function Qo(x,L,O,z,U){L.isScene!==!0&&(L=be),A.resetTextureUnits();const ne=L.fog,oe=z.isMeshStandardMaterial?L.environment:null,me=w===null?S.outputColorSpace:w.isXRRenderTarget===!0?w.texture.colorSpace:_n,ve=(z.isMeshStandardMaterial?F:v).get(z.envMap||oe),Ae=z.vertexColors===!0&&!!O.attributes.color&&O.attributes.color.itemSize===4,we=!!O.attributes.tangent&&(!!z.normalMap||z.anisotropy>0),Me=!!O.morphAttributes.position,$e=!!O.morphAttributes.normal,Je=!!O.morphAttributes.color;let tt=fn;z.toneMapped&&(w===null||w.isXRRenderTarget===!0)&&(tt=S.toneMapping);const Et=O.morphAttributes.position||O.morphAttributes.normal||O.morphAttributes.color,qe=Et!==void 0?Et.length:0,ye=Ee.get(z),ut=h.state.lights;if(b===!0&&(B===!0||x!==K)){const Ct=x===K&&z.id===I;te.setState(z,x,Ct)}let Ye=!1;z.version===ye.__version?(ye.needsLights&&ye.lightsStateVersion!==ut.state.version||ye.outputColorSpace!==me||U.isBatchedMesh&&ye.batching===!1||!U.isBatchedMesh&&ye.batching===!0||U.isBatchedMesh&&ye.batchingColor===!0&&U.colorTexture===null||U.isBatchedMesh&&ye.batchingColor===!1&&U.colorTexture!==null||U.isInstancedMesh&&ye.instancing===!1||!U.isInstancedMesh&&ye.instancing===!0||U.isSkinnedMesh&&ye.skinning===!1||!U.isSkinnedMesh&&ye.skinning===!0||U.isInstancedMesh&&ye.instancingColor===!0&&U.instanceColor===null||U.isInstancedMesh&&ye.instancingColor===!1&&U.instanceColor!==null||U.isInstancedMesh&&ye.instancingMorph===!0&&U.morphTexture===null||U.isInstancedMesh&&ye.instancingMorph===!1&&U.morphTexture!==null||ye.envMap!==ve||z.fog===!0&&ye.fog!==ne||ye.numClippingPlanes!==void 0&&(ye.numClippingPlanes!==te.numPlanes||ye.numIntersection!==te.numIntersection)||ye.vertexAlphas!==Ae||ye.vertexTangents!==we||ye.morphTargets!==Me||ye.morphNormals!==$e||ye.morphColors!==Je||ye.toneMapping!==tt||ye.morphTargetsCount!==qe)&&(Ye=!0):(Ye=!0,ye.__version=z.version);let Dt=ye.currentProgram;Ye===!0&&(Dt=Ai(z,L,U));let Dn=!1,yt=!1,xr=!1;const nt=Dt.getUniforms(),rn=ye.uniforms;if(Te.useProgram(Dt.program)&&(Dn=!0,yt=!0,xr=!0),z.id!==I&&(I=z.id,yt=!0),Dn||K!==x){Ie.reverseDepthBuffer?(ie.copy(x.projectionMatrix),lc(ie),cc(ie),nt.setValue(C,"projectionMatrix",ie)):nt.setValue(C,"projectionMatrix",x.projectionMatrix),nt.setValue(C,"viewMatrix",x.matrixWorldInverse);const Ct=nt.map.cameraPosition;Ct!==void 0&&Ct.setValue(C,ge.setFromMatrixPosition(x.matrixWorld)),Ie.logarithmicDepthBuffer&&nt.setValue(C,"logDepthBufFC",2/(Math.log(x.far+1)/Math.LN2)),(z.isMeshPhongMaterial||z.isMeshToonMaterial||z.isMeshLambertMaterial||z.isMeshBasicMaterial||z.isMeshStandardMaterial||z.isShaderMaterial)&&nt.setValue(C,"isOrthographic",x.isOrthographicCamera===!0),K!==x&&(K=x,yt=!0,xr=!0)}if(U.isSkinnedMesh){nt.setOptional(C,U,"bindMatrix"),nt.setOptional(C,U,"bindMatrixInverse");const Ct=U.skeleton;Ct&&(Ct.boneTexture===null&&Ct.computeBoneTexture(),nt.setValue(C,"boneTexture",Ct.boneTexture,A))}U.isBatchedMesh&&(nt.setOptional(C,U,"batchingTexture"),nt.setValue(C,"batchingTexture",U._matricesTexture,A),nt.setOptional(C,U,"batchingIdTexture"),nt.setValue(C,"batchingIdTexture",U._indirectTexture,A),nt.setOptional(C,U,"batchingColorTexture"),U._colorsTexture!==null&&nt.setValue(C,"batchingColorTexture",U._colorsTexture,A));const Mr=O.morphAttributes;if((Mr.position!==void 0||Mr.normal!==void 0||Mr.color!==void 0)&&Ce.update(U,O,Dt),(yt||ye.receiveShadow!==U.receiveShadow)&&(ye.receiveShadow=U.receiveShadow,nt.setValue(C,"receiveShadow",U.receiveShadow)),z.isMeshGouraudMaterial&&z.envMap!==null&&(rn.envMap.value=ve,rn.flipEnvMap.value=ve.isCubeTexture&&ve.isRenderTargetTexture===!1?-1:1),z.isMeshStandardMaterial&&z.envMap===null&&L.environment!==null&&(rn.envMapIntensity.value=L.environmentIntensity),yt&&(nt.setValue(C,"toneMappingExposure",S.toneMappingExposure),ye.needsLights&&el(rn,xr),ne&&z.fog===!0&&se.refreshFogUniforms(rn,ne),se.refreshMaterialUniforms(rn,z,Q,H,h.state.transmissionRenderTarget[x.id]),ir.upload(C,da(ye),rn,A)),z.isShaderMaterial&&z.uniformsNeedUpdate===!0&&(ir.upload(C,da(ye),rn,A),z.uniformsNeedUpdate=!1),z.isSpriteMaterial&&nt.setValue(C,"center",U.center),nt.setValue(C,"modelViewMatrix",U.modelViewMatrix),nt.setValue(C,"normalMatrix",U.normalMatrix),nt.setValue(C,"modelMatrix",U.matrixWorld),z.isShaderMaterial||z.isRawShaderMaterial){const Ct=z.uniformsGroups;for(let Sr=0,nl=Ct.length;Sr<nl;Sr++){const pa=Ct[Sr];P.update(pa,Dt),P.bind(pa,Dt)}}return Dt}function el(x,L){x.ambientLightColor.needsUpdate=L,x.lightProbe.needsUpdate=L,x.directionalLights.needsUpdate=L,x.directionalLightShadows.needsUpdate=L,x.pointLights.needsUpdate=L,x.pointLightShadows.needsUpdate=L,x.spotLights.needsUpdate=L,x.spotLightShadows.needsUpdate=L,x.rectAreaLights.needsUpdate=L,x.hemisphereLights.needsUpdate=L}function tl(x){return x.isMeshLambertMaterial||x.isMeshToonMaterial||x.isMeshPhongMaterial||x.isMeshStandardMaterial||x.isShadowMaterial||x.isShaderMaterial&&x.lights===!0}this.getActiveCubeFace=function(){return N},this.getActiveMipmapLevel=function(){return R},this.getRenderTarget=function(){return w},this.setRenderTargetTextures=function(x,L,O){Ee.get(x.texture).__webglTexture=L,Ee.get(x.depthTexture).__webglTexture=O;const z=Ee.get(x);z.__hasExternalTextures=!0,z.__autoAllocateDepthBuffer=O===void 0,z.__autoAllocateDepthBuffer||Ue.has("WEBGL_multisampled_render_to_texture")===!0&&(console.warn("THREE.WebGLRenderer: Render-to-texture extension was disabled because an external texture was provided"),z.__useRenderToTexture=!1)},this.setRenderTargetFramebuffer=function(x,L){const O=Ee.get(x);O.__webglFramebuffer=L,O.__useDefaultFramebuffer=L===void 0},this.setRenderTarget=function(x,L=0,O=0){w=x,N=L,R=O;let z=!0,U=null,ne=!1,oe=!1;if(x){const ve=Ee.get(x);if(ve.__useDefaultFramebuffer!==void 0)Te.bindFramebuffer(C.FRAMEBUFFER,null),z=!1;else if(ve.__webglFramebuffer===void 0)A.setupRenderTarget(x);else if(ve.__hasExternalTextures)A.rebindTextures(x,Ee.get(x.texture).__webglTexture,Ee.get(x.depthTexture).__webglTexture);else if(x.depthBuffer){const Me=x.depthTexture;if(ve.__boundDepthTexture!==Me){if(Me!==null&&Ee.has(Me)&&(x.width!==Me.image.width||x.height!==Me.image.height))throw new Error("WebGLRenderTarget: Attached DepthTexture is initialized to the incorrect size.");A.setupDepthRenderbuffer(x)}}const Ae=x.texture;(Ae.isData3DTexture||Ae.isDataArrayTexture||Ae.isCompressedArrayTexture)&&(oe=!0);const we=Ee.get(x).__webglFramebuffer;x.isWebGLCubeRenderTarget?(Array.isArray(we[L])?U=we[L][O]:U=we[L],ne=!0):x.samples>0&&A.useMultisampledRTT(x)===!1?U=Ee.get(x).__webglMultisampledFramebuffer:Array.isArray(we)?U=we[O]:U=we,g.copy(x.viewport),E.copy(x.scissor),k=x.scissorTest}else g.copy(ce).multiplyScalar(Q).floor(),E.copy(_e).multiplyScalar(Q).floor(),k=He;if(Te.bindFramebuffer(C.FRAMEBUFFER,U)&&z&&Te.drawBuffers(x,U),Te.viewport(g),Te.scissor(E),Te.setScissorTest(k),ne){const ve=Ee.get(x.texture);C.framebufferTexture2D(C.FRAMEBUFFER,C.COLOR_ATTACHMENT0,C.TEXTURE_CUBE_MAP_POSITIVE_X+L,ve.__webglTexture,O)}else if(oe){const ve=Ee.get(x.texture),Ae=L||0;C.framebufferTextureLayer(C.FRAMEBUFFER,C.COLOR_ATTACHMENT0,ve.__webglTexture,O||0,Ae)}I=-1},this.readRenderTargetPixels=function(x,L,O,z,U,ne,oe){if(!(x&&x.isWebGLRenderTarget)){console.error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let me=Ee.get(x).__webglFramebuffer;if(x.isWebGLCubeRenderTarget&&oe!==void 0&&(me=me[oe]),me){Te.bindFramebuffer(C.FRAMEBUFFER,me);try{const ve=x.texture,Ae=ve.format,we=ve.type;if(!Ie.textureFormatReadable(Ae)){console.error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!Ie.textureTypeReadable(we)){console.error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}L>=0&&L<=x.width-z&&O>=0&&O<=x.height-U&&C.readPixels(L,O,z,U,Pe.convert(Ae),Pe.convert(we),ne)}finally{const ve=w!==null?Ee.get(w).__webglFramebuffer:null;Te.bindFramebuffer(C.FRAMEBUFFER,ve)}}},this.readRenderTargetPixelsAsync=async function(x,L,O,z,U,ne,oe){if(!(x&&x.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let me=Ee.get(x).__webglFramebuffer;if(x.isWebGLCubeRenderTarget&&oe!==void 0&&(me=me[oe]),me){const ve=x.texture,Ae=ve.format,we=ve.type;if(!Ie.textureFormatReadable(Ae))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!Ie.textureTypeReadable(we))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");if(L>=0&&L<=x.width-z&&O>=0&&O<=x.height-U){Te.bindFramebuffer(C.FRAMEBUFFER,me);const Me=C.createBuffer();C.bindBuffer(C.PIXEL_PACK_BUFFER,Me),C.bufferData(C.PIXEL_PACK_BUFFER,ne.byteLength,C.STREAM_READ),C.readPixels(L,O,z,U,Pe.convert(Ae),Pe.convert(we),0);const $e=w!==null?Ee.get(w).__webglFramebuffer:null;Te.bindFramebuffer(C.FRAMEBUFFER,$e);const Je=C.fenceSync(C.SYNC_GPU_COMMANDS_COMPLETE,0);return C.flush(),await oc(C,Je,4),C.bindBuffer(C.PIXEL_PACK_BUFFER,Me),C.getBufferSubData(C.PIXEL_PACK_BUFFER,0,ne),C.deleteBuffer(Me),C.deleteSync(Je),ne}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")}},this.copyFramebufferToTexture=function(x,L=null,O=0){x.isTexture!==!0&&(nr("WebGLRenderer: copyFramebufferToTexture function signature has changed."),L=arguments[0]||null,x=arguments[1]);const z=Math.pow(2,-O),U=Math.floor(x.image.width*z),ne=Math.floor(x.image.height*z),oe=L!==null?L.x:0,me=L!==null?L.y:0;A.setTexture2D(x,0),C.copyTexSubImage2D(C.TEXTURE_2D,O,0,0,oe,me,U,ne),Te.unbindTexture()},this.copyTextureToTexture=function(x,L,O=null,z=null,U=0){x.isTexture!==!0&&(nr("WebGLRenderer: copyTextureToTexture function signature has changed."),z=arguments[0]||null,x=arguments[1],L=arguments[2],U=arguments[3]||0,O=null);let ne,oe,me,ve,Ae,we;O!==null?(ne=O.max.x-O.min.x,oe=O.max.y-O.min.y,me=O.min.x,ve=O.min.y):(ne=x.image.width,oe=x.image.height,me=0,ve=0),z!==null?(Ae=z.x,we=z.y):(Ae=0,we=0);const Me=Pe.convert(L.format),$e=Pe.convert(L.type);A.setTexture2D(L,0),C.pixelStorei(C.UNPACK_FLIP_Y_WEBGL,L.flipY),C.pixelStorei(C.UNPACK_PREMULTIPLY_ALPHA_WEBGL,L.premultiplyAlpha),C.pixelStorei(C.UNPACK_ALIGNMENT,L.unpackAlignment);const Je=C.getParameter(C.UNPACK_ROW_LENGTH),tt=C.getParameter(C.UNPACK_IMAGE_HEIGHT),Et=C.getParameter(C.UNPACK_SKIP_PIXELS),qe=C.getParameter(C.UNPACK_SKIP_ROWS),ye=C.getParameter(C.UNPACK_SKIP_IMAGES),ut=x.isCompressedTexture?x.mipmaps[U]:x.image;C.pixelStorei(C.UNPACK_ROW_LENGTH,ut.width),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,ut.height),C.pixelStorei(C.UNPACK_SKIP_PIXELS,me),C.pixelStorei(C.UNPACK_SKIP_ROWS,ve),x.isDataTexture?C.texSubImage2D(C.TEXTURE_2D,U,Ae,we,ne,oe,Me,$e,ut.data):x.isCompressedTexture?C.compressedTexSubImage2D(C.TEXTURE_2D,U,Ae,we,ut.width,ut.height,Me,ut.data):C.texSubImage2D(C.TEXTURE_2D,U,Ae,we,ne,oe,Me,$e,ut),C.pixelStorei(C.UNPACK_ROW_LENGTH,Je),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,tt),C.pixelStorei(C.UNPACK_SKIP_PIXELS,Et),C.pixelStorei(C.UNPACK_SKIP_ROWS,qe),C.pixelStorei(C.UNPACK_SKIP_IMAGES,ye),U===0&&L.generateMipmaps&&C.generateMipmap(C.TEXTURE_2D),Te.unbindTexture()},this.copyTextureToTexture3D=function(x,L,O=null,z=null,U=0){x.isTexture!==!0&&(nr("WebGLRenderer: copyTextureToTexture3D function signature has changed."),O=arguments[0]||null,z=arguments[1]||null,x=arguments[2],L=arguments[3],U=arguments[4]||0);let ne,oe,me,ve,Ae,we,Me,$e,Je;const tt=x.isCompressedTexture?x.mipmaps[U]:x.image;O!==null?(ne=O.max.x-O.min.x,oe=O.max.y-O.min.y,me=O.max.z-O.min.z,ve=O.min.x,Ae=O.min.y,we=O.min.z):(ne=tt.width,oe=tt.height,me=tt.depth,ve=0,Ae=0,we=0),z!==null?(Me=z.x,$e=z.y,Je=z.z):(Me=0,$e=0,Je=0);const Et=Pe.convert(L.format),qe=Pe.convert(L.type);let ye;if(L.isData3DTexture)A.setTexture3D(L,0),ye=C.TEXTURE_3D;else if(L.isDataArrayTexture||L.isCompressedArrayTexture)A.setTexture2DArray(L,0),ye=C.TEXTURE_2D_ARRAY;else{console.warn("THREE.WebGLRenderer.copyTextureToTexture3D: only supports THREE.DataTexture3D and THREE.DataTexture2DArray.");return}C.pixelStorei(C.UNPACK_FLIP_Y_WEBGL,L.flipY),C.pixelStorei(C.UNPACK_PREMULTIPLY_ALPHA_WEBGL,L.premultiplyAlpha),C.pixelStorei(C.UNPACK_ALIGNMENT,L.unpackAlignment);const ut=C.getParameter(C.UNPACK_ROW_LENGTH),Ye=C.getParameter(C.UNPACK_IMAGE_HEIGHT),Dt=C.getParameter(C.UNPACK_SKIP_PIXELS),Dn=C.getParameter(C.UNPACK_SKIP_ROWS),yt=C.getParameter(C.UNPACK_SKIP_IMAGES);C.pixelStorei(C.UNPACK_ROW_LENGTH,tt.width),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,tt.height),C.pixelStorei(C.UNPACK_SKIP_PIXELS,ve),C.pixelStorei(C.UNPACK_SKIP_ROWS,Ae),C.pixelStorei(C.UNPACK_SKIP_IMAGES,we),x.isDataTexture||x.isData3DTexture?C.texSubImage3D(ye,U,Me,$e,Je,ne,oe,me,Et,qe,tt.data):L.isCompressedArrayTexture?C.compressedTexSubImage3D(ye,U,Me,$e,Je,ne,oe,me,Et,tt.data):C.texSubImage3D(ye,U,Me,$e,Je,ne,oe,me,Et,qe,tt),C.pixelStorei(C.UNPACK_ROW_LENGTH,ut),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,Ye),C.pixelStorei(C.UNPACK_SKIP_PIXELS,Dt),C.pixelStorei(C.UNPACK_SKIP_ROWS,Dn),C.pixelStorei(C.UNPACK_SKIP_IMAGES,yt),U===0&&L.generateMipmaps&&C.generateMipmap(ye),Te.unbindTexture()},this.initRenderTarget=function(x){Ee.get(x).__webglFramebuffer===void 0&&A.setupRenderTarget(x)},this.initTexture=function(x){x.isCubeTexture?A.setTextureCube(x,0):x.isData3DTexture?A.setTexture3D(x,0):x.isDataArrayTexture||x.isCompressedArrayTexture?A.setTexture2DArray(x,0):A.setTexture2D(x,0),Te.unbindTexture()},this.resetState=function(){N=0,R=0,w=null,Te.reset(),je.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return tn}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(e){this._outputColorSpace=e;const t=this.getContext();t.drawingBufferColorSpace=e===Js?"display-p3":"srgb",t.unpackColorSpace=Ke.workingColorSpace===pr?"display-p3":"srgb"}}class ta{constructor(e,t=25e-5){this.isFogExp2=!0,this.name="",this.color=new Oe(e),this.density=t}clone(){return new ta(this.color,this.density)}toJSON(){return{type:"FogExp2",name:this.name,color:this.color.getHex(),density:this.density}}}class Sp extends ht{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new Xt,this.environmentIntensity=1,this.environmentRotation=new Xt,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(e,t){return super.copy(e,t),e.background!==null&&(this.background=e.background.clone()),e.environment!==null&&(this.environment=e.environment.clone()),e.fog!==null&&(this.fog=e.fog.clone()),this.backgroundBlurriness=e.backgroundBlurriness,this.backgroundIntensity=e.backgroundIntensity,this.backgroundRotation.copy(e.backgroundRotation),this.environmentIntensity=e.environmentIntensity,this.environmentRotation.copy(e.environmentRotation),e.overrideMaterial!==null&&(this.overrideMaterial=e.overrideMaterial.clone()),this.matrixAutoUpdate=e.matrixAutoUpdate,this}toJSON(e){const t=super.toJSON(e);return this.fog!==null&&(t.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(t.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(t.object.backgroundIntensity=this.backgroundIntensity),t.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(t.object.environmentIntensity=this.environmentIntensity),t.object.environmentRotation=this.environmentRotation.toArray(),t}}class na extends ri{constructor(e){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new Oe(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.linewidth=e.linewidth,this.linecap=e.linecap,this.linejoin=e.linejoin,this.fog=e.fog,this}}const ur=new D,hr=new D,co=new et,pi=new Fo,$i=new mr,ts=new D,uo=new D;class jo extends ht{constructor(e=new Rt,t=new na){super(),this.isLine=!0,this.type="Line",this.geometry=e,this.material=t,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[0];for(let r=1,s=t.count;r<s;r++)ur.fromBufferAttribute(t,r-1),hr.fromBufferAttribute(t,r),n[r]=n[r-1],n[r]+=ur.distanceTo(hr);e.setAttribute("lineDistance",new at(n,1))}else console.warn("THREE.Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(e,t){const n=this.geometry,r=this.matrixWorld,s=e.params.Line.threshold,a=n.drawRange;if(n.boundingSphere===null&&n.computeBoundingSphere(),$i.copy(n.boundingSphere),$i.applyMatrix4(r),$i.radius+=s,e.ray.intersectsSphere($i)===!1)return;co.copy(r).invert(),pi.copy(e.ray).applyMatrix4(co);const o=s/((this.scale.x+this.scale.y+this.scale.z)/3),l=o*o,c=this.isLineSegments?2:1,d=n.index,f=n.attributes.position;if(d!==null){const m=Math.max(0,a.start),_=Math.min(d.count,a.start+a.count);for(let M=m,h=_-1;M<h;M+=c){const u=d.getX(M),y=d.getX(M+1),S=Zi(this,e,pi,l,u,y);S&&t.push(S)}if(this.isLineLoop){const M=d.getX(_-1),h=d.getX(m),u=Zi(this,e,pi,l,M,h);u&&t.push(u)}}else{const m=Math.max(0,a.start),_=Math.min(f.count,a.start+a.count);for(let M=m,h=_-1;M<h;M+=c){const u=Zi(this,e,pi,l,M,M+1);u&&t.push(u)}if(this.isLineLoop){const M=Zi(this,e,pi,l,_-1,m);M&&t.push(M)}}}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const r=t[n[0]];if(r!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=r.length;s<a;s++){const o=r[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}}function Zi(i,e,t,n,r,s){const a=i.geometry.attributes.position;if(ur.fromBufferAttribute(a,r),hr.fromBufferAttribute(a,s),t.distanceSqToSegment(ur,hr,ts,uo)>n)return;ts.applyMatrix4(i.matrixWorld);const l=e.ray.origin.distanceTo(ts);if(!(l<e.near||l>e.far))return{distance:l,point:uo.clone().applyMatrix4(i.matrixWorld),index:r,face:null,faceIndex:null,barycoord:null,object:i}}const ho=new D,fo=new D;class Ep extends jo{constructor(e,t){super(e,t),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[];for(let r=0,s=t.count;r<s;r+=2)ho.fromBufferAttribute(t,r),fo.fromBufferAttribute(t,r+1),n[r]=r===0?0:n[r-1],n[r+1]=n[r]+ho.distanceTo(fo);e.setAttribute("lineDistance",new at(n,1))}else console.warn("THREE.LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class ia extends Rt{constructor(e=[],t=[],n=1,r=0){super(),this.type="PolyhedronGeometry",this.parameters={vertices:e,indices:t,radius:n,detail:r};const s=[],a=[];o(r),c(n),d(),this.setAttribute("position",new at(s,3)),this.setAttribute("normal",new at(s.slice(),3)),this.setAttribute("uv",new at(a,2)),r===0?this.computeVertexNormals():this.normalizeNormals();function o(y){const S=new D,T=new D,N=new D;for(let R=0;R<t.length;R+=3)m(t[R+0],S),m(t[R+1],T),m(t[R+2],N),l(S,T,N,y)}function l(y,S,T,N){const R=N+1,w=[];for(let I=0;I<=R;I++){w[I]=[];const K=y.clone().lerp(T,I/R),g=S.clone().lerp(T,I/R),E=R-I;for(let k=0;k<=E;k++)k===0&&I===R?w[I][k]=K:w[I][k]=K.clone().lerp(g,k/E)}for(let I=0;I<R;I++)for(let K=0;K<2*(R-I)-1;K++){const g=Math.floor(K/2);K%2===0?(f(w[I][g+1]),f(w[I+1][g]),f(w[I][g])):(f(w[I][g+1]),f(w[I+1][g+1]),f(w[I+1][g]))}}function c(y){const S=new D;for(let T=0;T<s.length;T+=3)S.x=s[T+0],S.y=s[T+1],S.z=s[T+2],S.normalize().multiplyScalar(y),s[T+0]=S.x,s[T+1]=S.y,s[T+2]=S.z}function d(){const y=new D;for(let S=0;S<s.length;S+=3){y.x=s[S+0],y.y=s[S+1],y.z=s[S+2];const T=h(y)/2/Math.PI+.5,N=u(y)/Math.PI+.5;a.push(T,1-N)}_(),p()}function p(){for(let y=0;y<a.length;y+=6){const S=a[y+0],T=a[y+2],N=a[y+4],R=Math.max(S,T,N),w=Math.min(S,T,N);R>.9&&w<.1&&(S<.2&&(a[y+0]+=1),T<.2&&(a[y+2]+=1),N<.2&&(a[y+4]+=1))}}function f(y){s.push(y.x,y.y,y.z)}function m(y,S){const T=y*3;S.x=e[T+0],S.y=e[T+1],S.z=e[T+2]}function _(){const y=new D,S=new D,T=new D,N=new D,R=new ze,w=new ze,I=new ze;for(let K=0,g=0;K<s.length;K+=9,g+=6){y.set(s[K+0],s[K+1],s[K+2]),S.set(s[K+3],s[K+4],s[K+5]),T.set(s[K+6],s[K+7],s[K+8]),R.set(a[g+0],a[g+1]),w.set(a[g+2],a[g+3]),I.set(a[g+4],a[g+5]),N.copy(y).add(S).add(T).divideScalar(3);const E=h(N);M(R,g+0,y,E),M(w,g+2,S,E),M(I,g+4,T,E)}}function M(y,S,T,N){N<0&&y.x===1&&(a[S]=y.x-1),T.x===0&&T.z===0&&(a[S]=N/2/Math.PI+.5)}function h(y){return Math.atan2(y.z,-y.x)}function u(y){return Math.atan2(-y.y,Math.sqrt(y.x*y.x+y.z*y.z))}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new ia(e.vertices,e.indices,e.radius,e.details)}}class dr extends ia{constructor(e=1,t=0){const n=(1+Math.sqrt(5))/2,r=[-1,n,0,1,n,0,-1,-n,0,1,-n,0,0,-1,n,0,1,n,0,-1,-n,0,1,-n,n,0,-1,n,0,1,-n,0,-1,-n,0,1],s=[0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1];super(r,s,e,t),this.type="IcosahedronGeometry",this.parameters={radius:e,detail:t}}static fromJSON(e){return new dr(e.radius,e.detail)}}class ra extends Rt{constructor(e=1,t=32,n=16,r=0,s=Math.PI*2,a=0,o=Math.PI){super(),this.type="SphereGeometry",this.parameters={radius:e,widthSegments:t,heightSegments:n,phiStart:r,phiLength:s,thetaStart:a,thetaLength:o},t=Math.max(3,Math.floor(t)),n=Math.max(2,Math.floor(n));const l=Math.min(a+o,Math.PI);let c=0;const d=[],p=new D,f=new D,m=[],_=[],M=[],h=[];for(let u=0;u<=n;u++){const y=[],S=u/n;let T=0;u===0&&a===0?T=.5/t:u===n&&l===Math.PI&&(T=-.5/t);for(let N=0;N<=t;N++){const R=N/t;p.x=-e*Math.cos(r+R*s)*Math.sin(a+S*o),p.y=e*Math.cos(a+S*o),p.z=e*Math.sin(r+R*s)*Math.sin(a+S*o),_.push(p.x,p.y,p.z),f.copy(p).normalize(),M.push(f.x,f.y,f.z),h.push(R+T,1-S),y.push(c++)}d.push(y)}for(let u=0;u<n;u++)for(let y=0;y<t;y++){const S=d[u][y+1],T=d[u][y],N=d[u+1][y],R=d[u+1][y+1];(u!==0||a>0)&&m.push(S,T,R),(u!==n-1||l<Math.PI)&&m.push(T,N,R)}this.setIndex(m),this.setAttribute("position",new at(_,3)),this.setAttribute("normal",new at(M,3)),this.setAttribute("uv",new at(h,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new ra(e.radius,e.widthSegments,e.heightSegments,e.phiStart,e.phiLength,e.thetaStart,e.thetaLength)}}class sa extends Rt{constructor(e=1,t=.4,n=12,r=48,s=Math.PI*2){super(),this.type="TorusGeometry",this.parameters={radius:e,tube:t,radialSegments:n,tubularSegments:r,arc:s},n=Math.floor(n),r=Math.floor(r);const a=[],o=[],l=[],c=[],d=new D,p=new D,f=new D;for(let m=0;m<=n;m++)for(let _=0;_<=r;_++){const M=_/r*s,h=m/n*Math.PI*2;p.x=(e+t*Math.cos(h))*Math.cos(M),p.y=(e+t*Math.cos(h))*Math.sin(M),p.z=t*Math.sin(h),o.push(p.x,p.y,p.z),d.x=e*Math.cos(M),d.y=e*Math.sin(M),f.subVectors(p,d).normalize(),l.push(f.x,f.y,f.z),c.push(_/r),c.push(m/n)}for(let m=1;m<=n;m++)for(let _=1;_<=r;_++){const M=(r+1)*m+_-1,h=(r+1)*(m-1)+_-1,u=(r+1)*(m-1)+_,y=(r+1)*m+_;a.push(M,h,y),a.push(h,u,y)}this.setIndex(a),this.setAttribute("position",new at(o,3)),this.setAttribute("normal",new at(l,3)),this.setAttribute("uv",new at(c,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new sa(e.radius,e.tube,e.radialSegments,e.tubularSegments,e.arc)}}class po extends ri{constructor(e){super(),this.isMeshStandardMaterial=!0,this.defines={STANDARD:""},this.type="MeshStandardMaterial",this.color=new Oe(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new Oe(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=Lo,this.normalScale=new ze(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new Xt,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.defines={STANDARD:""},this.color.copy(e.color),this.roughness=e.roughness,this.metalness=e.metalness,this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.emissive.copy(e.emissive),this.emissiveMap=e.emissiveMap,this.emissiveIntensity=e.emissiveIntensity,this.bumpMap=e.bumpMap,this.bumpScale=e.bumpScale,this.normalMap=e.normalMap,this.normalMapType=e.normalMapType,this.normalScale.copy(e.normalScale),this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.roughnessMap=e.roughnessMap,this.metalnessMap=e.metalnessMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.envMapIntensity=e.envMapIntensity,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.flatShading=e.flatShading,this.fog=e.fog,this}}class aa extends ht{constructor(e,t=1){super(),this.isLight=!0,this.type="Light",this.color=new Oe(e),this.intensity=t}dispose(){}copy(e,t){return super.copy(e,t),this.color.copy(e.color),this.intensity=e.intensity,this}toJSON(e){const t=super.toJSON(e);return t.object.color=this.color.getHex(),t.object.intensity=this.intensity,this.groundColor!==void 0&&(t.object.groundColor=this.groundColor.getHex()),this.distance!==void 0&&(t.object.distance=this.distance),this.angle!==void 0&&(t.object.angle=this.angle),this.decay!==void 0&&(t.object.decay=this.decay),this.penumbra!==void 0&&(t.object.penumbra=this.penumbra),this.shadow!==void 0&&(t.object.shadow=this.shadow.toJSON()),this.target!==void 0&&(t.object.target=this.target.uuid),t}}const ns=new et,mo=new D,_o=new D;class Jo{constructor(e){this.camera=e,this.intensity=1,this.bias=0,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new ze(512,512),this.map=null,this.mapPass=null,this.matrix=new et,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new Qs,this._frameExtents=new ze(1,1),this._viewportCount=1,this._viewports=[new Ze(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(e){const t=this.camera,n=this.matrix;mo.setFromMatrixPosition(e.matrixWorld),t.position.copy(mo),_o.setFromMatrixPosition(e.target.matrixWorld),t.lookAt(_o),t.updateMatrixWorld(),ns.multiplyMatrices(t.projectionMatrix,t.matrixWorldInverse),this._frustum.setFromProjectionMatrix(ns),n.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),n.multiply(ns)}getViewport(e){return this._viewports[e]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(e){return this.camera=e.camera.clone(),this.intensity=e.intensity,this.bias=e.bias,this.radius=e.radius,this.mapSize.copy(e.mapSize),this}clone(){return new this.constructor().copy(this)}toJSON(){const e={};return this.intensity!==1&&(e.intensity=this.intensity),this.bias!==0&&(e.bias=this.bias),this.normalBias!==0&&(e.normalBias=this.normalBias),this.radius!==1&&(e.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(e.mapSize=this.mapSize.toArray()),e.camera=this.camera.toJSON(!1).object,delete e.camera.matrix,e}}const go=new et,mi=new D,is=new D;class yp extends Jo{constructor(){super(new bt(90,1,.5,500)),this.isPointLightShadow=!0,this._frameExtents=new ze(4,2),this._viewportCount=6,this._viewports=[new Ze(2,1,1,1),new Ze(0,1,1,1),new Ze(3,1,1,1),new Ze(1,1,1,1),new Ze(3,0,1,1),new Ze(1,0,1,1)],this._cubeDirections=[new D(1,0,0),new D(-1,0,0),new D(0,0,1),new D(0,0,-1),new D(0,1,0),new D(0,-1,0)],this._cubeUps=[new D(0,1,0),new D(0,1,0),new D(0,1,0),new D(0,1,0),new D(0,0,1),new D(0,0,-1)]}updateMatrices(e,t=0){const n=this.camera,r=this.matrix,s=e.distance||n.far;s!==n.far&&(n.far=s,n.updateProjectionMatrix()),mi.setFromMatrixPosition(e.matrixWorld),n.position.copy(mi),is.copy(n.position),is.add(this._cubeDirections[t]),n.up.copy(this._cubeUps[t]),n.lookAt(is),n.updateMatrixWorld(),r.makeTranslation(-mi.x,-mi.y,-mi.z),go.multiplyMatrices(n.projectionMatrix,n.matrixWorldInverse),this._frustum.setFromProjectionMatrix(go)}}class Tp extends aa{constructor(e,t,n=0,r=2){super(e,t),this.isPointLight=!0,this.type="PointLight",this.distance=n,this.decay=r,this.shadow=new yp}get power(){return this.intensity*4*Math.PI}set power(e){this.intensity=e/(4*Math.PI)}dispose(){this.shadow.dispose()}copy(e,t){return super.copy(e,t),this.distance=e.distance,this.decay=e.decay,this.shadow=e.shadow.clone(),this}}class Ap extends Jo{constructor(){super(new Xo(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class bp extends aa{constructor(e,t){super(e,t),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(ht.DEFAULT_UP),this.updateMatrix(),this.target=new ht,this.shadow=new Ap}dispose(){this.shadow.dispose()}copy(e){return super.copy(e),this.target=e.target.clone(),this.shadow=e.shadow.clone(),this}}class wp extends aa{constructor(e,t){super(e,t),this.isAmbientLight=!0,this.type="AmbientLight"}}class Rp extends Ep{constructor(e=10,t=10,n=4473924,r=8947848){n=new Oe(n),r=new Oe(r);const s=t/2,a=e/t,o=e/2,l=[],c=[];for(let f=0,m=0,_=-o;f<=t;f++,_+=a){l.push(-o,0,_,o,0,_),l.push(_,0,-o,_,0,o);const M=f===s?n:r;M.toArray(c,m),m+=3,M.toArray(c,m),m+=3,M.toArray(c,m),m+=3,M.toArray(c,m),m+=3}const d=new Rt;d.setAttribute("position",new at(l,3)),d.setAttribute("color",new at(c,3));const p=new na({vertexColors:!0,toneMapped:!1});super(d,p),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:Xs}}));typeof window<"u"&&(window.__THREE__?console.warn("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=Xs);const Cp={class:"situation"},Pp={class:"sit-header"},Lp={class:"h-left"},Dp={class:"h-right"},Up={class:"clock"},Ip={class:"kpi-row"},Np={class:"kpi-value"},Fp={class:"kpi-label"},Op={class:"sit-body"},Bp={class:"col left"},zp={class:"card"},Hp={class:"rank-list"},Gp=["onClick"],Vp={class:"rk"},kp={class:"tn"},Wp={class:"sc"},Xp={class:"card"},qp={class:"monitor-list"},Yp={class:"m-name"},Kp={class:"m-team"},$p={class:"m-state"},Zp={class:"col center"},jp={class:"col right"},Jp={class:"card"},Qp={class:"card"},em={class:"event-list"},tm={class:"e-time"},nm={class:"e-text"},im={__name:"Situation3D",props:{contestId:{type:[String,Number],default:null}},setup(i){const e=i,t=cl(),n=Ut(e.contestId?Number(e.contestId):null),r=Ut([]),s=Ut({}),a=Ut([]),o=Ut([]),l=Ut([]),c=Ut([]),d=Ut(""),p=Ut(!1),f=Ut(null),m=Ut(null);let _=null,M=null,h=null,u=null,y=0,S=null;const T=ll(()=>[{key:"teams",label:"参赛战队",value:s.value.teamCount??a.value.length??0},{key:"targets",label:"在线靶机",value:s.value.targetCount??o.value.filter(b=>b.status!=="DOWN").length},{key:"attacks",label:"本轮攻击",value:s.value.attackCount??0},{key:"solves",label:"解题总数",value:s.value.solveCount??0},{key:"first",label:"一血数",value:s.value.firstBloodCount??0}]);function N(){d.value=new Date().toLocaleString("zh-CN",{hour12:!1})}function R(b,B){l.value.unshift({id:++y,type:b,text:B,time:new Date().toLocaleTimeString("zh-CN",{hour12:!1})}),l.value.length>60&&l.value.pop()}function w(b){const B=b.teamId||b.id;B&&t.push(`/team/${B}`)}function I(){const b=f.value;if(!b)return;const B=b.clientWidth,ie=b.clientHeight,Z=new Sp;Z.fog=new ta(330264,.02);const ge=new bt(50,B/ie,.1,1e3);ge.position.set(0,24,46),ge.lookAt(0,0,0);const ee=new Mp({antialias:!0,alpha:!0});ee.setPixelRatio(Math.min(window.devicePixelRatio,2)),ee.setSize(B,ie),b.appendChild(ee.domElement),Z.add(new wp(8952251,.7));const be=new bp(16777215,1.1);be.position.set(20,40,20),Z.add(be);const Ne=new Tp(5090295,1.4,120);Ne.position.set(-20,10,-20),Z.add(Ne);const Se=new Rp(120,40,1981023,1188408);Z.add(Se);const C=new wt(new dr(3,1),new po({color:5090295,emissive:1723002,roughness:.3,metalness:.6,wireframe:!1}));C.position.y=6,Z.add(C);const it=new wt(new dr(4.2,1),new cr({color:5090295,wireframe:!0,transparent:!0,opacity:.22}));it.position.y=6,Z.add(it);const Ue=new gi;Z.add(Ue);const Ie=new sa(20,.06,8,120),Te=new cr({color:2781096,transparent:!0,opacity:.35}),ke=new wt(Ie,Te);ke.rotation.x=Math.PI/2,ke.position.y=1,Z.add(ke);const Ee=ke.clone();Ee.rotation.z=Math.PI/4,Ee.scale.setScalar(.7),Ee.position.y=8,Z.add(Ee),_={scene:Z,camera:ge,renderer:ee,nodeGroup:Ue,core:C,halo:it,nodes:[],spherical:{radius:52,theta:0,phi:Math.PI/3},drag:!1,lastX:0,lastY:0},b.addEventListener("mousedown",K),b.addEventListener("mousemove",E),b.addEventListener("mouseup",g),b.addEventListener("mouseleave",g),b.addEventListener("wheel",k,{passive:!1}),G()}function K(b){_.drag=!0,_.lastX=b.clientX,_.lastY=b.clientY}function g(){_.drag=!1}function E(b){if(!(_!=null&&_.drag))return;const B=b.clientX-_.lastX,ie=b.clientY-_.lastY;_.lastX=b.clientX,_.lastY=b.clientY,_.spherical.theta-=B*.006,_.spherical.phi=Math.max(.25,Math.min(Math.PI/2.2,_.spherical.phi-ie*.004))}function k(b){b.preventDefault(),_&&(_.spherical.radius=Math.max(26,Math.min(110,_.spherical.radius+b.deltaY*.05)))}function G(){if(S=requestAnimationFrame(G),!_)return;const b=performance.now()/1e3,B=_.spherical,ie=B.radius*Math.sin(B.phi)*Math.cos(B.theta),Z=B.radius*Math.cos(B.phi)+6,ge=B.radius*Math.sin(B.phi)*Math.sin(B.theta);_.camera.position.set(ie,Z,ge),_.camera.lookAt(0,6,0),_.core.rotation.y=b*.25,_.core.rotation.x=b*.12,_.halo.rotation.y=-b*.18,_.halo.rotation.z=b*.1,_.nodeGroup.rotation.y=b*.05;for(const ee of _.nodes){if(!ee.userData||!ee.userData.pulse)continue;const be=1+Math.sin(b*2.2+ee.userData.phase)*.06;ee.scale.setScalar(be)}_.renderer.render(_.scene,_.camera)}function X(b){var ge,ee,be,Ne;if(!_)return;for(const Se of _.nodes)_.nodeGroup.remove(Se),(ee=(ge=Se.geometry)==null?void 0:ge.dispose)==null||ee.call(ge),(Ne=(be=Se.material)==null?void 0:be.dispose)==null||Ne.call(be);_.nodes=[];const B=Array.isArray(b)?b:(b==null?void 0:b.nodes)||[];B.length||B.push(...a.value.slice(0,10).map((Se,C)=>({id:Se.teamId||Se.id||C,name:Se.teamName||Se.name||`战队${C+1}`,score:Se.score||0,type:"TEAM"})));const ie=B.length||1,Z=20;B.forEach((Se,C)=>{const it=Number(Se.score??Se.value??0),Ue=Math.max(.7,Math.min(2.6,.7+it/600)),Ie=Se.status==="DOWN"?16072511:Se.status==="ATTACKED"?16743680:Se.type==="TARGET"?16766011:5090295,Te=new ra(Ue,16,16),ke=new po({color:Ie,emissive:Ie,emissiveIntensity:.45,roughness:.35,metalness:.5}),Ee=new wt(Te,ke),A=C/ie*Math.PI*2,v=Se.type==="TARGET"?-2:2;Ee.position.set(Math.cos(A)*Z,v+Math.sin(C*1.7)*2,Math.sin(A)*Z),Ee.userData={id:Se.id,name:Se.name,score:it,type:Se.type,pulse:!0,phase:C*.8},_.nodeGroup.add(Ee),_.nodes.push(Ee);const F=[Ee.position.clone(),new D(0,6,0)],Y=new Rt().setFromPoints(F),j=new na({color:Ie,transparent:!0,opacity:.18}),q=new jo(Y,j);_.nodeGroup.add(q),_.nodes.push(q)})}function J(b){if(!M)return;const B=(b==null?void 0:b.rounds)||(b==null?void 0:b.labels)||[],ie=((b==null?void 0:b.series)||(b==null?void 0:b.datasets)||[]).map(Z=>({name:Z.name||Z.teamName||"攻击",type:"line",smooth:!0,showSymbol:!1,areaStyle:{opacity:.12},data:Z.data||Z.values||[]}));M.setOption({tooltip:{trigger:"axis"},legend:{textStyle:{color:"#a8b7cc",fontSize:10},top:0,type:"scroll"},grid:{left:36,right:12,top:30,bottom:22},xAxis:{type:"category",data:B.length?B:Array.from({length:12},(Z,ge)=>`R${ge+1}`),axisLabel:{color:"#86909c",fontSize:10},axisLine:{lineStyle:{color:"#e5e6eb"}}},yAxis:{type:"value",axisLabel:{color:"#86909c",fontSize:10},splitLine:{lineStyle:{color:"#ffffff"}}},series:ie.length?ie:[{name:"攻击次数",type:"line",smooth:!0,showSymbol:!1,areaStyle:{opacity:.15},data:Array.from({length:12},()=>0)}]},!0)}async function H(){await Promise.all([Q(),W(),le(),ce()])}async function Q(){try{const b=await dl(n.value?{contestId:n.value}:void 0);s.value=b||{},b!=null&&b.trend&&J(b.trend)}catch{}}async function W(){try{const b=await fl(n.value?{contestId:n.value,limit:10}:{limit:10});a.value=Array.isArray(b)?b:(b==null?void 0:b.records)||(b==null?void 0:b.list)||[]}catch{a.value=[]}}async function le(){try{const b=await pl(n.value?{contestId:n.value}:void 0);c.value=Array.isArray(b)?b:(b==null?void 0:b.nodes)||[],X(c.value)}catch{X([])}}async function ce(){try{const b=await ml(n.value?{contestId:n.value}:void 0);o.value=(Array.isArray(b)?b:(b==null?void 0:b.records)||[]).map(B=>({id:B.id,name:B.name||B.hostname||`靶机-${B.id}`,teamName:B.teamName||B.ownerTeam||"—",status:(B.status||"UNKNOWN").toUpperCase()}))}catch{o.value=[]}}async function _e(){try{const b=await _l({size:50});r.value=Array.isArray(b)?b:(b==null?void 0:b.records)||[],!n.value&&r.value.length&&(n.value=r.value[0].id)}catch{}}function He(){try{const b=n.value?`/ws/contest/${n.value}`:"/ws/contest/0";h=gl(b,B=>{const ie=(B.type||"").toUpperCase(),Z=B.data||{};p.value=!0,ie==="SCORE_UPDATE"?(R("SCORE",`${Z.teamName||"战队"} 得分更新 ${Z.score??""}`),W(),le()):ie==="TARGET_DOWN"?(R("DOWN",`靶机 ${Z.targetName||Z.name||""} 宕机`),ce(),le()):ie==="TARGET_UP"?(R("INFO",`靶机 ${Z.targetName||Z.name||""} 恢复`),ce()):ie==="FIRST_BLOOD"?(R("FIRST",`一血：${Z.teamName||""} ${Z.targetName||""}`),Q()):ie==="ANNOUNCE"&&R("INFO",Z.content||String(Z))}),setTimeout(()=>{p.value=!0},800)}catch{p.value=!1}}function Ve(){if(_&&f.value){const b=f.value.clientWidth,B=f.value.clientHeight;_.camera.aspect=b/B,_.camera.updateProjectionMatrix(),_.renderer.setSize(b,B)}M==null||M.resize()}return rl(async()=>{N(),u=setInterval(N,1e3),await _e(),await ul(),I(),M=hl(m.value),await H(),He(),window.addEventListener("resize",Ve),R("INFO","态势大屏已接入实时数据流")}),sl(()=>{if(clearInterval(u),h==null||h.close(),cancelAnimationFrame(S),window.removeEventListener("resize",Ve),_){const b=f.value;b&&(b.removeEventListener("mousedown",K),b.removeEventListener("mousemove",E),b.removeEventListener("mouseup",g),b.removeEventListener("mouseleave",g),b.removeEventListener("wheel",k),_.renderer.domElement.parentNode===b&&b.removeChild(_.renderer.domElement)),_.renderer.dispose()}M==null||M.dispose()}),(b,B)=>{const ie=yr("el-option"),Z=yr("el-select"),ge=yr("el-empty");return gt(),Gt("div",Cp,[Fe("header",Pp,[Fe("div",Lp,[Fe("span",{class:bi(["dot",p.value?"on":"off"])},null,2),B[1]||(B[1]=Fe("h1",{class:"h-title"},"网络安全态势感知大屏",-1))]),Fe("div",Dp,[Fe("span",Up,It(d.value),1),al(Z,{modelValue:n.value,"onUpdate:modelValue":B[0]||(B[0]=ee=>n.value=ee),size:"small",style:{width:"180px"},placeholder:"选择赛事",onChange:H},{default:ol(()=>[(gt(!0),Gt(ai,null,oi(r.value,ee=>(gt(),wi(ie,{key:ee.id,label:ee.name,value:ee.id},null,8,["label","value"]))),128))]),_:1},8,["modelValue"])])]),Fe("section",Ip,[(gt(!0),Gt(ai,null,oi(T.value,ee=>(gt(),Gt("div",{class:"kpi",key:ee.key},[Fe("div",Np,It(ee.value),1),Fe("div",Fp,It(ee.label),1)]))),128))]),Fe("div",Op,[Fe("aside",Bp,[Fe("div",zp,[B[2]||(B[2]=Fe("div",{class:"card-title"},"实时排行榜 TOP10",-1)),Fe("div",Hp,[(gt(!0),Gt(ai,null,oi(a.value,(ee,be)=>(gt(),Gt("div",{key:ee.teamId||ee.id,class:bi(["rank-row",`r${be+1}`]),onClick:Ne=>w(ee)},[Fe("span",Vp,It(be+1),1),Fe("span",kp,It(ee.teamName||ee.name),1),Fe("span",Wp,It(ee.score),1)],10,Gp))),128)),a.value.length?Er("",!0):(gt(),wi(ge,{key:0,description:"暂无数据","image-size":50}))])]),Fe("div",Xp,[B[3]||(B[3]=Fe("div",{class:"card-title"},"靶机监控",-1)),Fe("div",qp,[(gt(!0),Gt(ai,null,oi(o.value,ee=>(gt(),Gt("div",{key:ee.id,class:bi(["monitor-row",ee.status.toLowerCase()])},[Fe("span",Yp,It(ee.name),1),Fe("span",Kp,It(ee.teamName),1),Fe("span",$p,It(ee.status),1)],2))),128)),o.value.length?Er("",!0):(gt(),wi(ge,{key:0,description:"暂无监控数据","image-size":50}))])])]),Fe("main",Zp,[Fe("div",{ref_key:"threeHost",ref:f,class:"three-host"},null,512),B[4]||(B[4]=Fe("div",{class:"three-hint"},"拖拽旋转 · 滚轮缩放 · 点击节点查看详情",-1))]),Fe("aside",jp,[Fe("div",Jp,[B[5]||(B[5]=Fe("div",{class:"card-title"},"攻击趋势（近 12 轮）",-1)),Fe("div",{ref_key:"trendRef",ref:m,class:"chart"},null,512)]),Fe("div",Qp,[B[6]||(B[6]=Fe("div",{class:"card-title"},"实时事件",-1)),Fe("div",em,[(gt(!0),Gt(ai,null,oi(l.value,ee=>(gt(),Gt("div",{key:ee.id,class:bi(["event",ee.type.toLowerCase()])},[Fe("span",tm,It(ee.time),1),Fe("span",nm,It(ee.text),1)],2))),128)),l.value.length?Er("",!0):(gt(),wi(ge,{key:0,description:"暂无事件","image-size":50}))])])])])])}}},cm=il(im,[["__scopeId","data-v-58731f34"]]);export{cm as default};
