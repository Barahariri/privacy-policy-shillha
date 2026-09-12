package com.alkhalijialawal.employee;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(this), "Android");

        String html = "<!doctype html>\n<html lang=\"ar\" dir=\"rtl\">\n<head>\n<meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,viewport-fit=cover\">\n<title>الخليجي الأول - موظف الطلبات</title>\n<style>\n:root{font-family:system-ui,-apple-system,\"Segoe UI\",Tahoma,Arial}\n*{box-sizing:border-box}body{margin:0;background:#f5f6f8;color:#171717}\n.wrap{max-width:680px;margin:auto;padding:14px}.card{background:#fff;border:1px solid #e5e7eb;border-radius:18px;padding:16px;margin:11px 0;box-shadow:0 2px 10px #0000000b}\nh1{font-size:24px;margin:4px 0}h2{font-size:17px;margin:0 0 10px}.muted{color:#666;font-size:13px;line-height:1.7}\nlabel{display:block;font-weight:700;margin:10px 0 5px}input,textarea{width:100%;font:inherit;border:1px solid #ccd1d8;border-radius:12px;padding:11px;background:#fff}\ntextarea{min-height:145px;resize:vertical}.grid{display:grid;grid-template-columns:1fr 1fr;gap:9px}\nbutton{width:100%;border:0;border-radius:13px;padding:13px;font:inherit;font-weight:800;cursor:pointer;margin-top:9px}\n.primary{background:#111;color:#fff}.green{background:#1aa260;color:#fff}.soft{background:#eef1f4}.warn{background:#fff7e6;color:#7a4b00}\n.code{font-size:34px;font-weight:900;text-align:center;letter-spacing:1px;padding:11px;border:2px dashed #111;border-radius:14px;margin:10px 0}\n.ok{font-weight:800;color:#177245}.hidden{display:none}.pill{display:inline-block;background:#eef1f4;border-radius:999px;padding:5px 9px;margin:3px;font-size:12px}\n@media(max-width:520px){.grid{grid-template-columns:1fr}.wrap{padding:9px}}\n</style>\n</head>\n<body><div class=\"wrap\">\n<div class=\"card\"><h1>الخليجي الأول</h1><div class=\"muted\">برنامج الموظف — عنوان الزبونة حتى لو بعثته بأكثر من رسالة</div></div>\n\n<div class=\"card\">\n<h2>إعداد مرة واحدة</h2>\n<div class=\"grid\">\n<div><label>اسم الموظف</label><input id=\"employee\" placeholder=\"مثال: زهير\"></div>\n<div><label>رمز الموظف</label><input id=\"prefix\" inputmode=\"numeric\" maxlength=\"2\" placeholder=\"مثال: 1\"></div>\n</div>\n\n<button class=\"soft\" onclick=\"saveSettings()\">حفظ الإعدادات</button>\n</div>\n\n<div class=\"card\">\n<h2>1) اجمع رسائل الزبونة</h2>\n<div class=\"muted\">يفهم الصياغة العادية والمختصرة والعربي والإنجليزي. أمثلة: حطين ق1 ش2 م3 66559944 — أو Hateen blk 1 st 2 house 3 66559944. وإذا العنوان والتلفون برسالتين، الصقهما تحت بعض.</div>\n<textarea id=\"raw\" placeholder=\"مثال:\nالجهراء قطعة 3 شارع 14 منزل 22\n\nثم الصق تحتها رسالة ثانية:\n99999999\"></textarea>\n<button class=\"primary\" onclick=\"parseRaw()\">فهم وترتيب كل الرسائل</button>\n<button class=\"soft\" onclick=\"clearRaw()\">مسح الرسائل</button>\n</div>\n\n<div class=\"card\">\n<h2>2) راجع البيانات</h2>\n<div class=\"grid\">\n<div><label>الاسم</label><input id=\"name\"></div>\n<div><label>الهاتف</label><input id=\"phone\" inputmode=\"tel\"></div>\n<div><label>المنطقة</label><input id=\"area\"></div>\n<div><label>القطعة</label><input id=\"block\"></div>\n<div><label>الشارع</label><input id=\"street\"></div>\n<div><label>الجادة</label><input id=\"avenue\"></div>\n<div><label>المنزل / العمارة</label><input id=\"house\"></div>\n<div><label>ملاحظة</label><input id=\"note\"></div>\n</div>\n<div class=\"muted\" id=\"detected\"></div>\n<button class=\"green\" onclick=\"createAndSend()\">إنشاء رمز الطلب ونسخ البيانات</button>\n</div>\n\n<div class=\"card hidden\" id=\"done\">\n<div class=\"ok\">تم إنشاء رمز الطلب</div>\n<div class=\"code\" id=\"orderDisplay\">#1-101</div>\n<div class=\"muted\">هذا هو <b>رمز الطلب</b> الذي تكتبه على الملابس. رقم الطلب الحقيقي سيأتي لاحقاً من فاتورة الأمين.</div>\n\n<button class=\"soft\" onclick=\"copyMessage()\">نسخ البيانات مرة أخرى</button>\n<button class=\"soft\" onclick=\"newOrder()\">طلب جديد</button>\n</div>\n\n<div class=\"card\">\n<h2>العداد</h2>\n<div class=\"grid\">\n<div><label>الرمز التالي</label><input id=\"counter\" type=\"number\" min=\"1\"></div>\n<div style=\"align-self:end\"><button class=\"soft\" onclick=\"saveCounter()\">حفظ</button></div>\n</div>\n<div class=\"muted\">مثال: زهير رمز الموظف 1 → #1-101، عمر 2 → #2-101.</div>\n</div>\n</div>\n\n<script>\nconst $=id=>document.getElementById(id);\nlet lastMessage='',lastCode='';\nfunction latinDigits(s){\n  return String(s||'')\n   .replace(/[٠-٩]/g,d=>'٠١٢٣٤٥٦٧٨٩'.indexOf(d))\n   .replace(/[۰-۹]/g,d=>'۰۱۲۳۴۵۶۷۸۹'.indexOf(d));\n}\nfunction onlyDigits(s){return latinDigits(s).replace(/[^\\d]/g,'')}\nfunction saveSettings(){\n localStorage.k1_employee=$('employee').value.trim();\n localStorage.k1_prefix=onlyDigits($('prefix').value).slice(0,2)||'1';\n if(!localStorage.k1_counter)localStorage.k1_counter='101';\n loadSettings(); alert('تم الحفظ');\n}\nfunction loadSettings(){\n $('employee').value=localStorage.k1_employee||'';\n $('prefix').value=localStorage.k1_prefix||'1';\n $('counter').value=localStorage.k1_counter||'101';\n}\nfunction findLabel(text,labels){\n for(const lab of labels){\n   const re=new RegExp('(?:^|\\\\n)\\\\s*(?:'+lab+')\\\\s*[:：\\\\-]?\\\\s*([^\\\\n]+)','i');\n   const m=text.match(re); if(m)return m[1].trim();\n }\n return '';\n}\nfunction findNumberAfter(text,labels){\n for(const lab of labels){\n   const re=new RegExp('(?:'+lab+')\\\\s*[:：\\\\-]?\\\\s*([0-9٠-٩۰-۹]+)','i');\n   const m=text.match(re); if(m)return onlyDigits(m[1]);\n }\n return '';\n}\nfunction parseRaw(){\n let t=latinDigits($('raw').value.replace(/\\r/g,'\\n'));\n let low=t.toLowerCase();\n\n // Normalize common punctuation/separators while preserving line breaks.\n let normalized=low\n   .replace(/[،,;|]+/g,' ')\n   .replace(/\\s+/g,' ')\n   .trim();\n\n // ---------- PHONE ----------\n let phone=onlyDigits(findLabel(t,['(?:رقم\\\\s*)?(?:التلفون|الهاتف|الجوال|الموبايل|phone|mobile|tel)']));\n if(!phone){\n   const matches=[...t.matchAll(/(?:\\+?965[\\s\\-]?)?([4569]\\d{7})(?!\\d)/g)];\n   if(matches.length) phone=matches[matches.length-1][1];\n }\n if(!phone){\n   const matches=[...t.matchAll(/(?<!\\d)(\\d{8,12})(?!\\d)/g)];\n   if(matches.length) phone=matches[matches.length-1][1];\n }\n $('phone').value=phone;\n\n // ---------- NAME / NOTE ----------\n $('name').value=findLabel(t,['الاسم','اسم\\\\s*العميل','name','customer']);\n $('note').value=findLabel(t,['ملاحظة','الملاحظة','note','notes']);\n\n // ---------- EXPLICIT LABELS ----------\n $('area').value=findLabel(t,['المنطقة','منطقة','area','region']);\n $('block').value=findNumberAfter(t,['القطعة','قطعة','block','blk']);\n $('street').value=findNumberAfter(t,['الشارع','شارع','street','st']);\n $('avenue').value=findNumberAfter(t,['الجادة','جادة','avenue','ave']);\n $('house').value=findNumberAfter(t,['المنزل','البيت','العمارة','منزل','بيت','عمارة','بناية','house','home','building','bldg']);\n\n // ---------- ARABIC SHORT FORM ----------\n // Examples: حطين ق1 ش2 م3   /   حطين ق 1 ش 2 م 3\n if(!$('block').value){\n   let mm=normalized.match(/(?:^|\\s)(?:ق|قطة|قطعه|قطعة)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('block').value=mm[1];\n }\n if(!$('street').value){\n   let mm=normalized.match(/(?:^|\\s)(?:ش|شارع)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('street').value=mm[1];\n }\n if(!$('avenue').value){\n   let mm=normalized.match(/(?:^|\\s)(?:ج|جادة)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('avenue').value=mm[1];\n }\n if(!$('house').value){\n   let mm=normalized.match(/(?:^|\\s)(?:م|منزل|بيت|عمارة|بناية)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('house').value=mm[1];\n }\n\n // ---------- ENGLISH SHORT FORM ----------\n // Examples: Hateen blk 1 st 2 house 3\n if(!$('block').value){\n   let mm=normalized.match(/(?:^|\\s)(?:blk|block)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('block').value=mm[1];\n }\n if(!$('street').value){\n   let mm=normalized.match(/(?:^|\\s)(?:st|street)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('street').value=mm[1];\n }\n if(!$('avenue').value){\n   let mm=normalized.match(/(?:^|\\s)(?:ave|avenue)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('avenue').value=mm[1];\n }\n if(!$('house').value){\n   let mm=normalized.match(/(?:^|\\s)(?:house|home|bldg|building)\\s*[:\\-]?\\s*(\\d+)/i);\n   if(mm)$('house').value=mm[1];\n }\n\n // ---------- AREA FALLBACK ----------\n // Take text before the first address marker / phone.\n if(!$('area').value){\n   let cleaned=normalized\n      .replace(/(?:\\+?965[\\s-]?)?[4569]\\d{7}/g,' ')\n      .replace(/(?<!\\d)\\d{8,12}(?!\\d)/g,' ')\n      .trim();\n\n   let firstMarkerIndex=cleaned.length;\n   const markers=[\n     /\\s(?:ق|قطعة|قطعه)\\s*\\d+/i,\n     /\\s(?:ش|شارع)\\s*\\d+/i,\n     /\\s(?:م|منزل|بيت|عمارة|بناية)\\s*\\d+/i,\n     /\\s(?:ج|جادة)\\s*\\d+/i,\n     /\\s(?:blk|block)\\s*\\d+/i,\n     /\\s(?:st|street)\\s*\\d+/i,\n     /\\s(?:ave|avenue)\\s*\\d+/i,\n     /\\s(?:house|home|building|bldg)\\s*\\d+/i\n   ];\n   for(const rg of markers){\n      const mm=cleaned.match(rg);\n      if(mm && mm.index < firstMarkerIndex) firstMarkerIndex=mm.index;\n   }\n   let areaCandidate=cleaned.slice(0,firstMarkerIndex).trim();\n   areaCandidate=areaCandidate.replace(/^(?:العنوان|عنوان|address|area|region)\\s*[:：-]?\\s*/i,'').trim();\n\n   // If the first line is useful, prefer it.\n   if(!areaCandidate){\n      const first=t.split('\\n').map(x=>x.trim()).filter(Boolean)\n        .find(x=>/[ء-يA-Za-z]/.test(x) && !/\\d{7,}/.test(onlyDigits(x)));\n      if(first) areaCandidate=first.split(/(?:قطعة|شارع|جادة|منزل|بيت|عمارة|بناية|blk|block|st|street|ave|avenue|house|home|building|bldg)/i)[0].trim();\n   }\n   if(areaCandidate && areaCandidate.length<=60)$('area').value=areaCandidate;\n }\n\n // ---------- CLEAN AREA ----------\n $('area').value=$('area').value\n   .replace(/^(?:المنطقة|منطقة|area|region)\\s*[:：-]?\\s*/i,'')\n   .trim();\n\n // ---------- DETECTION CHIPS ----------\n const chips=[];\n if($('phone').value)chips.push('✓ الهاتف');\n if($('area').value)chips.push('✓ المنطقة');\n if($('block').value)chips.push('✓ القطعة');\n if($('street').value)chips.push('✓ الشارع');\n if($('avenue').value)chips.push('✓ الجادة');\n if($('house').value)chips.push('✓ المنزل');\n $('detected').innerHTML=chips.map(x=>'<span class=\"pill\">'+x+'</span>').join('');\n}\nfunction nextCode(){\n const prefix=onlyDigits($('prefix').value)||'1';\n let c=parseInt(localStorage.k1_counter||$('counter').value||'101',10);\n if(!Number.isFinite(c)||c<1)c=101;\n lastCode=prefix+'-'+String(c).padStart(3,'0');\n localStorage.k1_counter=String(c+1); $('counter').value=String(c+1);\n return lastCode;\n}\nfunction buildMessage(code){\n return `[K1ORDER]\nرمز الطلب: ${code}\nالموظف: ${$('employee').value.trim()}\nالاسم: ${$('name').value.trim()}\nالهاتف: ${onlyDigits($('phone').value)}\nالمنطقة: ${$('area').value.trim()}\nالقطعة: ${$('block').value.trim()}\nالشارع: ${$('street').value.trim()}\nالجادة: ${$('avenue').value.trim()}\nالمنزل: ${$('house').value.trim()}\nملاحظة: ${$('note').value.trim()}\n[/K1ORDER]`;\n}\nfunction createAndSend(){\n if(!$('employee').value.trim()||!onlyDigits($('prefix').value)){alert('احفظ اسم الموظف ورمزه أولاً');return}\n parseRaw();\n if(!$('area').value.trim() && !onlyDigits($('phone').value)){alert('لم أجد منطقة أو هاتف. راجع البيانات قبل إنشاء الرمز.');return}\n const code=nextCode(); lastMessage=buildMessage(code);\n $('orderDisplay').textContent='#'+code; $('done').classList.remove('hidden');\n $('done').scrollIntoView({behavior:'smooth'});\n copyMessage(true);\n}\nasync function copyMessage(autoCopy=false){\n if(!lastMessage)return;\n const msg=autoCopy ? 'تم إنشاء رمز الطلب ونسخ البيانات. اكتب الرمز على الملابس، والبيانات جاهزة للصق.' : 'تم نسخ بيانات الطلب';\n try{await navigator.clipboard.writeText(lastMessage);alert(msg)}catch(e){\n   const ta=document.createElement('textarea');ta.value=lastMessage;document.body.appendChild(ta);ta.select();document.execCommand('copy');ta.remove();alert(msg);\n }\n}\nfunction clearRaw(){$('raw').value='';$('detected').innerHTML=''}\nfunction newOrder(){\n ['raw','name','phone','area','block','street','avenue','house','note'].forEach(x=>$(x).value='');\n $('detected').innerHTML='';$('done').classList.add('hidden');window.scrollTo({top:0,behavior:'smooth'});\n}\nfunction saveCounter(){\n let c=parseInt($('counter').value,10);if(!Number.isFinite(c)||c<1){alert('رقم غير صحيح');return}\n localStorage.k1_counter=String(c);alert('تم حفظ العداد');\n}\nloadSettings();\n</script>\n</body></html>";

        // الصفحة مدمجة داخل التطبيق نفسه، بدون الاعتماد على android_asset/index.html
        webView.loadDataWithBaseURL(
            "https://k1.local/",
            html,
            "text/html",
            "UTF-8",
            null
        );
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static class AndroidBridge {
        private final Context context;

        AndroidBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void copyText(String text) {
            ClipboardManager cm =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(
                ClipData.newPlainText("K1 Order", text == null ? "" : text)
            );
        }
    }
}
