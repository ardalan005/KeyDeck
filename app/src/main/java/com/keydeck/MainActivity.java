package com.keydeck;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    SharedPreferences sp; Link link; KbView kv;

    static class Theme {
        String n; int bg1, bg2, key, tx, mod, acc; boolean glow;
        Theme(String n, int bg1, int bg2, int key, int tx, int mod, int acc, boolean glow) {
            this.n = n; this.bg1 = bg1; this.bg2 = bg2; this.key = key; this.tx = tx; this.mod = mod; this.acc = acc; this.glow = glow;
        }
    }
    static final Theme[] TH = {
        new Theme("Classic Black", 0xFF101114, 0xFF1A1B20, 0xFF202228, 0xFFF2F2F2, 0xFF2B2D34, 0xFF3DA9FC, false),
        new Theme("Neon Night", 0xFF0B0F1A, 0xFF1B1245, 0xFF1B2340, 0xFFE8EEFC, 0xFF2A3466, 0xFF22D3EE, true),
        new Theme("Aurora", 0xFF04201F, 0xFF0B3D3A, 0xFF0F3A38, 0xFFE0FFF6, 0xFF14524E, 0xFF34D399, false),
        new Theme("Sunset", 0xFF2A0F2E, 0xFF4A1D1D, 0xFF3D1F3A, 0xFFFFE9E0, 0xFF5A2A3F, 0xFFFF7A59, false),
        new Theme("Ocean Glass", 0xFF06182C, 0xFF0A3A5C, 0xFF12344F, 0xFFE6F6FF, 0xFF1B4B70, 0xFF4CC9F0, true),
        new Theme("Mono Light", 0xFFE9EDF3, 0xFFCFD6E2, 0xFFFFFFFF, 0xFF1D2433, 0xFFDDE3EE, 0xFF4F46E5, false),
        new Theme("Matrix", 0xFF020A04, 0xFF04180B, 0xFF07200F, 0xFF6DFF9A, 0xFF0B3318, 0xFF00FF66, true),
        new Theme("Rose Gold", 0xFF2B1B22, 0xFF3D2430, 0xFF4A2D3A, 0xFFFFE8F0, 0xFF5F3A4B, 0xFFF4A6C0, false),
        new Theme("Graphite", 0xFF121212, 0xFF1F1F1F, 0xFF2A2A2A, 0xFFF1F1F1, 0xFF3A3A3A, 0xFFFFB703, false)
    };
    static final String[][] DEF = {
        {"Task Mgr", "key", "ctrl+shift+esc"}, {"Desktop", "key", "win+d"}, {"Snip", "key", "win+shift+s"}, {"Lock", "key", "win+l"},
        {"Copy", "key", "ctrl+c"}, {"Paste", "key", "ctrl+v"}, {"Undo", "key", "ctrl+z"},
        {"Browser", "run", "https://www.google.com"}, {"Files", "run", "explorer"}, {"Calc", "run", "calc"}
    };

    static final int[] LSC = {0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x56};
    static final String[] AR_N = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "\u0636", "\u0635", "\u062B", "\u0642", "\u0641", "\u063A", "\u0639", "\u0647", "\u062E", "\u062D", "\u062C", "\u062F", "\u0634", "\u0633", "\u064A", "\u0628", "\u0644", "\u0627", "\u062A", "\u0646", "\u0645", "\u0643", "\u0637", "\u0630", "\\", "\u0626", "\u0621", "\u0624", "\u0631", "\u0644\u0627", "\u0649", "\u0629", "\u0648", "\u0632", "\u0638", "\\"};
    static final String[] AR_S = {"!", "@", "#", "$", "%", "^", "&", "*", ")", "(", "_", "+", "\u064E", "\u064B", "\u064F", "\u064C", "\u0644\u0625", "\u0625", "\u2018", "\u00F7", "\u00D7", "\u061B", "<", ">", "\u0650", "\u064D", "]", "[", "\u0644\u0623", "\u0623", "\u0640", "\u060C", "/", ":", "\"", "\u0651", "|", "~", "\u0652", "}", "{", "\u0644\u0622", "\u0622", "\u2019", ",", ".", "\u061F", "|"};
    static final String[] KU_N = {"\u0661", "\u0662", "\u0663", "\u0664", "\u0665", "\u0666", "\u0667", "\u0668", "\u0669", "\u0660", "-", "=", "\u0642", "\u0648", "\u06D5", "\u0631", "\u062A", "\u06CC", "\u0626", "\u062D", "\u06C6", "\u067E", "]", "[", "\u0627", "\u0633", "\u062F", "\u0641", "\u06AF", "\u0647", "\u0698", "\u06A9", "\u0644", "\u061B", "'", "`", "\\", "\u0632", "\u062E", "\u062C", "\u06A4", "\u0628", "\u0646", "\u0645", "\u060C", ".", "/", "\\"};
    static final String[] KU_S = {"!", "@", "#", "$", "\u066A", "^", "&", "*", ")", "(", "_", "+", "`", "\u0648\u0648", "\u064A", "\u0695", "\u0637", "\u06CE", "\u0621", "\u0639", "\u0624", "\u062B", "}", "{", "\u0622", "\u0634", "\u0630", "\u0625", "\u063A", "\u200C", "\u0623", "\u0643", "\u06B5", ":", "\"", "~", "|", "\u0636", "\u0635", "\u0686", "\u0638", "\u0649", "\u0629", "\u0640", ">", "<", "\u061F", "/"};
    static final String[] LANGS = {"EN", "AR", "KU"};

    static class K {
        String l, s, act, fl, uni; int vk, kind, idx, st, pend, sc, fsc; float u, x, y, w, h, x2, y2, w2, h2; boolean on, rep, useFn, fnKey; long t0, t1;
        K(String l, String s, int vk, float u, int kind) { this.l = l; this.s = s; this.vk = vk; this.u = u; this.kind = kind; }
    }

    static class Link {
        final SharedPreferences sp; final LinkedBlockingQueue<String> q = new LinkedBlockingQueue<String>();
        volatile boolean run = true, ok = false; volatile String status = "Not connected"; volatile int epoch = 0; Closeable sock;
        Link(SharedPreferences sp) { this.sp = sp; }
        void start() { Thread t = new Thread(new Runnable() { public void run() { loop(); } }); t.setDaemon(true); t.start(); }
        void reconnect() { epoch++; close(); }
        void close() { try { if (sock != null) sock.close(); } catch (Exception e) { } }
        void send(String s) { if (ok) q.offer(s + "\n"); }
        void nap(long ms) { try { Thread.sleep(ms); } catch (Exception e) { } }
        String line(InputStream in) throws IOException {
            StringBuilder sb = new StringBuilder(); int ch;
            while ((ch = in.read()) != -1 && ch != '\n' && sb.length() < 20) sb.append((char) ch);
            return sb.toString().trim();
        }
        void loop() {
            while (run) {
                int ep = epoch;
                try {
                    String mode = sp.getString("mode", "wifi"); String pin = sp.getString("pin", "");
                    OutputStream os; boolean tcp = true;
                    status = "Connecting...";
                    if (mode.equals("bt")) {
                        BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter(); String addr = sp.getString("bt", "");
                        if (ad == null || addr.length() == 0) { status = "Choose a Bluetooth PC"; nap(1500); continue; }
                        ad.cancelDiscovery();
                        BluetoothSocket bs = ad.getRemoteDevice(addr).createRfcommSocketToServiceRecord(SPP);
                        sock = bs; bs.connect(); os = bs.getOutputStream(); tcp = false;
                    } else {
                        String host = mode.equals("usb") ? "127.0.0.1" : sp.getString("host", "").trim();
                        if (host.length() == 0) { status = "Open Connect and enter the PC address"; nap(1500); continue; }
                        int port = 8766;
                        if (host.contains(":")) { String[] p = host.split(":"); host = p[0]; port = Integer.parseInt(p[1]); }
                        Socket s = new Socket(); sock = s; s.connect(new InetSocketAddress(host, port), 4000); s.setTcpNoDelay(true); s.setSoTimeout(3000);
                        os = s.getOutputStream();
                        os.write(("AUTH " + pin + "\n").getBytes("UTF-8")); os.flush();
                        if (!line(s.getInputStream()).startsWith("OK")) { status = "Wrong PIN"; close(); nap(3000); continue; }
                    }
                    if (!tcp) { os.write(("AUTH " + pin + "\n").getBytes("UTF-8")); os.flush(); }
                    q.clear(); ok = true; status = "Connected";
                    while (run && ep == epoch) {
                        String m = q.poll(4, TimeUnit.SECONDS); if (m == null) m = "P\n";
                        os.write(m.getBytes("UTF-8")); os.flush();
                    }
                } catch (Exception e) { status = "Offline - retrying"; }
                ok = false; close(); nap(1500);
            }
        }
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("kd", 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 31) requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT"}, 1);
        link = new Link(sp); link.start();
        kv = new KbView(this); setContentView(kv);
    }
    @Override public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override protected void onDestroy() { link.run = false; link.close(); super.onDestroy(); }
    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
    LinearLayout col() { LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(20), dp(10), dp(20), dp(10)); return c; }

    void themeDialog() {
        LinearLayout c = col();
        for (int i = 0; i < TH.length; i++) {
            final int ii = i; Button b = new Button(this); b.setText(TH[i].n); b.setTextColor(TH[i].acc); b.setBackgroundColor(TH[i].bg2);
            b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { kv.setTheme(ii); } });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, dp(4), 0, dp(4)); c.addView(b, lp);
        }
        TextView tv = new TextView(this); tv.setText("Accent color (far left = theme default)"); tv.setPadding(0, dp(12), 0, 0); c.addView(tv);
        SeekBar sb = new SeekBar(this); sb.setMax(360); sb.setProgress(kv.hue);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { kv.setHue(p); }
            public void onStartTrackingTouch(SeekBar s) { }
            public void onStopTrackingTouch(SeekBar s) { }
        });
        c.addView(sb);
        CheckBox cb = new CheckBox(this); cb.setText("Vibrate on key press"); cb.setChecked(kv.hap);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton x, boolean on) { kv.hap = on; sp.edit().putBoolean("hap", on).apply(); }
        });
        c.addView(cb);
        ScrollView sv = new ScrollView(this); sv.addView(c);
        new AlertDialog.Builder(this).setTitle("Themes and style").setView(sv).setPositiveButton("Done", null).show();
    }

    void editDeck(final int i) {
        final String[] d = kv.dk[i]; LinearLayout c = col();
        final EditText l = new EditText(this); l.setHint("Label"); l.setText(d[0]);
        final Spinner ty = new Spinner(this);
        ty.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Press keys (example: ctrl+shift+esc)", "Open program or link", "Type text"}));
        ty.setSelection(d[1].equals("key") ? 0 : d[1].equals("run") ? 1 : 2);
        final EditText v = new EditText(this); v.setHint("Keys, program or text"); v.setText(d[2]);
        c.addView(l); c.addView(ty); c.addView(v);
        new AlertDialog.Builder(this).setTitle("Edit button " + (i + 1)).setView(c)
            .setPositiveButton("Save", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dlg, int w) {
                    d[0] = l.getText().toString(); d[1] = new String[]{"key", "run", "text"}[ty.getSelectedItemPosition()]; d[2] = v.getText().toString();
                    kv.saveDeck(); kv.invalidate();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    void connDialog() {
        LinearLayout c = col();
        final String[] hints = {"Run the KeyDeck receiver on the PC and type the IP address it shows. Same Wi-Fi.",
            "Enable USB debugging, plug the cable in and keep the PC receiver running. It sets up the USB link by itself.",
            "Pair tablet and PC in Bluetooth settings. On Windows add an INCOMING COM port, then put it in config.json (bt_com)."};
        final Spinner m = new Spinner(this);
        m.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Wi-Fi", "USB cable", "Bluetooth"}));
        String md = sp.getString("mode", "wifi"); m.setSelection(md.equals("usb") ? 1 : md.equals("bt") ? 2 : 0);
        final EditText host = new EditText(this); host.setHint("PC address, example 192.168.1.20"); host.setText(sp.getString("host", ""));
        final EditText pin = new EditText(this); pin.setHint("PIN shown on the PC"); pin.setInputType(InputType.TYPE_CLASS_NUMBER); pin.setText(sp.getString("pin", ""));
        final List<String> names = new ArrayList<String>(), addrs = new ArrayList<String>();
        try {
            BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
            if (ad != null) for (BluetoothDevice bd : ad.getBondedDevices()) { names.add(bd.getName() + "  " + bd.getAddress()); addrs.add(bd.getAddress()); }
        } catch (Exception e) { }
        if (names.isEmpty()) names.add("No paired devices found");
        final Spinner bt = new Spinner(this);
        bt.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names));
        int bi = addrs.indexOf(sp.getString("bt", "")); if (bi >= 0) bt.setSelection(bi);
        final TextView hint = new TextView(this); hint.setPadding(0, dp(10), 0, 0);
        m.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                host.setVisibility(pos == 0 ? View.VISIBLE : View.GONE); bt.setVisibility(pos == 2 ? View.VISIBLE : View.GONE); hint.setText(hints[pos]);
            }
            public void onNothingSelected(AdapterView<?> a) { }
        });
        c.addView(m); c.addView(host); c.addView(bt); c.addView(pin); c.addView(hint);
        new AlertDialog.Builder(this).setTitle("Connect to PC").setView(c)
            .setPositiveButton("Connect", new android.content.DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface dlg, int w) {
                    SharedPreferences.Editor e = sp.edit(); int p = m.getSelectedItemPosition();
                    e.putString("mode", p == 1 ? "usb" : p == 2 ? "bt" : "wifi").putString("host", host.getText().toString().trim()).putString("pin", pin.getText().toString().trim());
                    int b2 = bt.getSelectedItemPosition(); if (b2 < addrs.size()) e.putString("bt", addrs.get(b2));
                    e.apply(); link.reconnect();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    class KbView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG), t = new Paint(Paint.ANTI_ALIAS_FLAG); RectF rf = new RectF(), r2 = new RectF(); Path pa = new Path();
        List<List<K>> rows = new ArrayList<List<K>>(); List<K> all = new ArrayList<K>(), top = new ArrayList<K>(), deck = new ArrayList<K>(), mods = new ArrayList<K>();
        K chip, slider, enter, fnKey, langBtn; int lang; HashMap<Integer, String[]> arM = new HashMap<Integer, String[]>(), kuM = new HashMap<Integer, String[]>(); final float[] RH = {.85f, 1, 1, 1, 1, 1};
        HashMap<Integer, K> act = new HashMap<Integer, K>(); HashMap<Integer, Float> lx = new HashMap<Integer, Float>(); HashMap<Integer, Runnable> reps = new HashMap<Integer, Runnable>();
        HashSet<Integer> fired = new HashSet<Integer>(); Handler h = new Handler(Looper.getMainLooper());
        int th, hue; boolean hap; float vol = .5f, ux, uy, ox, oy, sacc; Shader bgs; String[][] dk = new String[10][3];

        KbView(Context cx) {
            super(cx); th = sp.getInt("th", 0); hue = sp.getInt("hue", 0); hap = sp.getBoolean("hap", true);
            t.setTypeface(Typeface.DEFAULT_BOLD); loadDeck(); build(); lang = sp.getInt("lang", 0);
            for (int i = 0; i < LSC.length; i++) { arM.put(LSC[i], new String[]{AR_N[i], AR_S[i]}); kuM.put(LSC[i], new String[]{KU_N[i], KU_S[i]}); }
            h.post(new Runnable() { public void run() { invalidate(); h.postDelayed(this, 700); } });
        }
        float dp(float v) { return v * getResources().getDisplayMetrics().density; }
        int acc() { return hue == 0 ? TH[th].acc : Color.HSVToColor(new float[]{hue % 360, .65f, 1f}); }
        float lum(int c) { return (0.299f * Color.red(c) + 0.587f * Color.green(c) + 0.114f * Color.blue(c)) / 255f; }
        void setTheme(int i) { th = i; sp.edit().putInt("th", i).apply(); makeBg(); invalidate(); }
        void setHue(int v) { hue = v; sp.edit().putInt("hue", v).apply(); invalidate(); }
        void makeBg() { if (getHeight() > 0) bgs = new LinearGradient(0, 0, 0, getHeight(), TH[th].bg1, TH[th].bg2, Shader.TileMode.CLAMP); }
        boolean fnOn() { return fnKey != null && (fnKey.st > 0 || fnKey.on); }

        void loadDeck() {
            for (int i = 0; i < 10; i++) dk[i] = DEF[i].clone();
            try {
                JSONArray a = new JSONArray(sp.getString("deck", "[]"));
                for (int i = 0; i < a.length() && i < 10; i++) { JSONObject o = a.getJSONObject(i); dk[i] = new String[]{o.getString("l"), o.getString("t"), o.getString("v")}; }
            } catch (Exception e) { }
        }
        void saveDeck() {
            try {
                JSONArray a = new JSONArray();
                for (String[] d : dk) { JSONObject o = new JSONObject(); o.put("l", d[0]); o.put("t", d[1]); o.put("v", d[2]); a.put(o); }
                sp.edit().putString("deck", a.toString()).apply();
            } catch (Exception e) { }
        }
        K a(List<K> r, String l, String s, int vk, float u, int kind) { K k = new K(l, s, vk, u, kind); r.add(k); all.add(k); if (kind == 1) mods.add(k); return k; }
        K s(List<K> r, String l, String sub, int sc, float u, int kind) { K k = a(r, l, sub, 0, u, kind); k.sc = sc; return k; }
        K f(K k, int fsc, String fl) { k.fsc = fsc; k.fl = fl; return k; }
        void g(List<K> r, float u) { r.add(new K(null, null, 0, u, -1)); }
        void build() {
            List<K> r = new ArrayList<K>(); float fw = 14.15f / 15f;
            s(r, "Esc", null, 0x01, 1, 7);
            int[] fs = {0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43, 0x44, 0x57, 0x58};
            for (int i = 0; i < 12; i++) { K k = s(r, "F" + (i + 1), null, fs[i], fw, 7); if (i == 9) f(k, 0x46, "ScrLk"); }
            s(r, "PrtSc", null, 0x137, fw, 7); a(r, "NumLk", null, 0x90, fw, 7); a(r, "Pause", null, 0x13, fw, 7); s(r, "Del", null, 0x153, 1.25f, 7); rows.add(r);
            r = new ArrayList<K>(); s(r, "`", "\u00AC", 0x29, 1, 0);
            String nd = "1234567890", ns = "!\"\u00A3$%^&*()"; int[] nf = {0, 0, 0, 0, 0, 0, 0x47, 0x48, 0x49, 0x37}; String[] nl = {null, null, null, null, null, null, "7", "8", "9", "*"};
            for (int i = 0; i < 10; i++) { K k = s(r, "" + nd.charAt(i), "" + ns.charAt(i), 0x02 + i, 1, 0); if (nf[i] != 0) f(k, nf[i], nl[i]); }
            s(r, "-", "_", 0x0C, 1, 0); s(r, "=", "+", 0x0D, 1, 0); s(r, "Backspace", null, 0x0E, 2.15f, 7); s(r, "Home", null, 0x147, 1.25f, 7); rows.add(r);
            r = new ArrayList<K>(); s(r, "Tab", null, 0x0F, 1.5f, 7); String q = "QWERTYUIOP"; int[] qf = {0, 0, 0, 0, 0, 0, 0x4B, 0x4C, 0x4D, 0x4A}; String[] ql = {null, null, null, null, null, null, "4", "5", "6", "-"};
            for (int i = 0; i < 10; i++) { K k = s(r, "" + q.charAt(i), null, 0x10 + i, 1, 0); if (qf[i] != 0) f(k, qf[i], ql[i]); }
            s(r, "[", "{", 0x1A, 1, 0); s(r, "]", "}", 0x1B, 1, 0); enter = s(r, "Enter", null, 0x1C, 1.65f, 7); s(r, "PgUp", null, 0x149, 1.25f, 7); rows.add(r);
            r = new ArrayList<K>(); s(r, "Caps Lock", null, 0x3A, 1.75f, 7); String s2 = "ASDFGHJKL"; String[] jl = {null, null, null, null, null, null, "1", "2", "3"}; int[] jf = {0, 0, 0, 0, 0, 0, 0x4F, 0x50, 0x51};
            for (int i = 0; i < 9; i++) { K k = s(r, "" + s2.charAt(i), null, 0x1E + i, 1, 0); if (jf[i] != 0) f(k, jf[i], jl[i]); }
            f(s(r, ";", ":", 0x27, 1, 0), 0x4E, "+"); s(r, "'", "@", 0x28, 1, 0); s(r, "#", "~", 0x2B, 1, 0); g(r, 1.4f); s(r, "PgDn", null, 0x151, 1.25f, 7); rows.add(r);
            r = new ArrayList<K>(); s(r, "Shift", null, 0x2A, 1.25f, 1); s(r, "\\", "|", 0x56, 1, 0); String z = "ZXCVBNM";
            for (int i = 0; i < 7; i++) { K k = s(r, "" + z.charAt(i), null, 0x2C + i, 1, 0); if (i == 6) f(k, 0x52, "0"); }
            s(r, ",", "<", 0x33, 1, 0); f(s(r, ".", ">", 0x34, 1, 0), 0x53, "."); f(s(r, "/", "?", 0x35, 1, 0), 0x135, "/");
            s(r, "Shift", null, 0x36, 1.9f, 1); s(r, "\u2191", null, 0x148, 1, 7); s(r, "End", null, 0x14F, 1.25f, 7); rows.add(r);
            r = new ArrayList<K>(); s(r, "Ctrl", null, 0x1D, 1.25f, 1); fnKey = s(r, "FN", null, 0, 1, 1); fnKey.fnKey = true; s(r, "Win", null, 0x15B, 1, 1); s(r, "Alt", null, 0x38, 1.25f, 1);
            s(r, "", null, 0x39, 4.65f, 0); s(r, "Alt", null, 0x138, 1, 1); s(r, "Menu", null, 0x15D, 1, 7); s(r, "Ctrl", null, 0x11D, 1, 1); s(r, "Insert", null, 0x152, 1, 7);
            s(r, "\u2190", null, 0x14B, 1, 7); s(r, "\u2193", null, 0x150, 1, 7); s(r, "\u2192", null, 0x14D, 1.25f, 7); rows.add(r);
            chip = a(top, "", null, 0, 3.4f, 3); chip.act = "conn"; g(top, .3f);
            a(top, "", null, 0xB1, 1.1f, 2); a(top, "", null, 0xB3, 1.1f, 2); a(top, "", null, 0xB0, 1.1f, 2); a(top, "", null, 0xAD, 1.1f, 2); g(top, .3f);
            a(top, "", null, 0xAE, 1.1f, 2).rep = true; slider = a(top, "", null, 0, 2.5f, 5); a(top, "", null, 0xAF, 1.1f, 2).rep = true; g(top, .3f);
            langBtn = a(top, "", null, 0, 1.2f, 3); langBtn.act = "lang"; K tb = a(top, "THEME", null, 0, 1.6f, 3); tb.act = "theme";
            for (int i = 0; i < 10; i++) { K k = new K("", null, 0, 1, 4); k.idx = i; deck.add(k); }
        }
        @Override protected void onSizeChanged(int W, int H, int ow, int oh) {
            float pad = W * 0.01f, kw = 16.4f; ux = (W - 2 * pad) / kw; uy = Math.min(H / 8.8f, ux * 1.18f);
            float tw = kw * ux; ox = (W - tw) / 2; oy = (H - 8.8f * uy) / 2;
            float y = oy + .2f * uy, gp = .12f * ux, bw = (tw - 9 * gp) / 10, x0 = ox;
            for (K k : top) { k.x = x0; k.y = y; k.w = k.u * ux; k.h = .85f * uy; x0 += k.w; }
            y += 1.1f * uy;
            for (int i = 0; i < 10; i++) { K k = deck.get(i); k.x = ox + i * (bw + gp); k.y = y; k.w = bw; k.h = 1.05f * uy; }
            y += 1.3f * uy; float[] ry = new float[rows.size()];
            for (int ri = 0; ri < rows.size(); ri++) {
                float x = ox, rh = RH[ri] * uy; ry[ri] = y;
                for (K k : rows.get(ri)) { k.x = x; k.y = y; k.w = k.u * ux; k.h = rh; x += k.w; }
                y += rh; if (ri == 0) y += .15f * uy;
            }
            enter.x2 = ox + 13.75f * ux; enter.y2 = ry[3]; enter.w2 = 1.4f * ux; enter.h2 = RH[3] * uy;
            makeBg();
        }
        void txt(Canvas c, String s, float cx, float cy, float maxW, float size, int col, Paint.Align al) {
            t.setColor(col); t.setTextAlign(al); t.setTextSize(size); float w = t.measureText(s); if (w > maxW && w > 0) t.setTextSize(size * maxW / w);
            c.drawText(s, cx, cy + t.getTextSize() * .35f, t);
        }
        void tri(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3, int col) {
            pa.reset(); pa.moveTo(x1, y1); pa.lineTo(x2, y2); pa.lineTo(x3, y3); pa.close(); p.setStyle(Paint.Style.FILL); p.setColor(col); c.drawPath(pa, p);
        }
        void icon(Canvas c, int vk, float cx, float cy, float s, int col) {
            p.setStyle(Paint.Style.FILL); p.setColor(col);
            if (vk == 0xB1) { c.drawRect(cx - s, cy - s * .8f, cx - s * .7f, cy + s * .8f, p); tri(c, cx + s, cy - s * .8f, cx + s, cy + s * .8f, cx - s * .6f, cy, col); }
            else if (vk == 0xB0) { c.drawRect(cx + s * .7f, cy - s * .8f, cx + s, cy + s * .8f, p); tri(c, cx - s, cy - s * .8f, cx - s, cy + s * .8f, cx + s * .6f, cy, col); }
            else if (vk == 0xB3) { tri(c, cx - s * .9f, cy - s * .8f, cx - s * .9f, cy + s * .8f, cx + s * .1f, cy, col); c.drawRect(cx + s * .4f, cy - s * .8f, cx + s * .65f, cy + s * .8f, p); c.drawRect(cx + s * .85f, cy - s * .8f, cx + s * 1.1f, cy + s * .8f, p); }
            else {
                pa.reset(); pa.moveTo(cx - s, cy - s * .35f); pa.lineTo(cx - s * .4f, cy - s * .35f); pa.lineTo(cx + s * .2f, cy - s * .85f); pa.lineTo(cx + s * .2f, cy + s * .85f);
                pa.lineTo(cx - s * .4f, cy + s * .35f); pa.lineTo(cx - s, cy + s * .35f); pa.close(); c.drawPath(pa, p);
                p.setStrokeWidth(dp(2.2f));
                if (vk == 0xAE || vk == 0xAF) { c.drawLine(cx + s * .55f, cy, cx + s * 1.05f, cy, p); if (vk == 0xAF) c.drawLine(cx + s * .8f, cy - s * .25f, cx + s * .8f, cy + s * .25f, p); }
                else { c.drawLine(cx + s * .5f, cy - s * .35f, cx + s * 1.05f, cy + s * .35f, p); c.drawLine(cx + s * .5f, cy + s * .35f, cx + s * 1.05f, cy - s * .35f, p); }
            }
        }
        void shape(Canvas c, K k, float m, float r) {
            if (k == enter) {
                float xl = k.x + m, xr = k.x + k.w - m, yt = k.y + m, ym = k.y + k.h, yb = k.y2 + k.h2 - m, xl2 = k.x2 + m;
                pa.reset(); pa.moveTo(xl, yt); pa.lineTo(xr, yt); pa.lineTo(xr, yb); pa.lineTo(xl2, yb); pa.lineTo(xl2, ym); pa.lineTo(xl, ym); pa.close();
                p.setPathEffect(new CornerPathEffect(r)); c.drawPath(pa, p); p.setPathEffect(null);
            } else c.drawRoundRect(rf, r, r, p);
        }
        void drawK(Canvas c, K k, Theme T, int ac) {
            float m = Math.min(k.w, k.h) * .06f; rf.set(k.x + m, k.y + m, k.x + k.w - m, k.y + k.h - m); float r = Math.min(rf.width(), rf.height()) * .16f;
            boolean fn = fnOn(), hi = k.on || k.st > 0; int fill = hi ? ac : (k.kind == 0 ? T.key : T.mod);
            p.setStyle(Paint.Style.FILL); p.setColor(fill); shape(c, k, m, r);
            if (T.glow) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.4f)); p.setColor(((hi ? 0xFFFFFFFF : ac) & 0x00FFFFFF) | 0xAA000000); shape(c, k, m, r); p.setStyle(Paint.Style.FILL); }
            int tc = hi ? (lum(ac) > .6f ? 0xFF101010 : 0xFFFFFFFF) : T.tx; float cx = k.x + k.w / 2, cy = k.y + k.h / 2;
            if (k.kind == 2) { icon(c, k.vk, cx, cy, k.h * .26f, hi ? tc : ac); return; }
            if (k.kind == 5) {
                float tx0 = k.x + k.w * .12f, tx1 = k.x + k.w * .88f; rf.set(tx0, cy - dp(3), tx1, cy + dp(3)); p.setColor((T.tx & 0x00FFFFFF) | 0x44000000); c.drawRoundRect(rf, dp(3), dp(3), p);
                rf.set(tx0, cy - dp(3), tx0 + (tx1 - tx0) * vol, cy + dp(3)); p.setColor(ac); c.drawRoundRect(rf, dp(3), dp(3), p);
                c.drawCircle(tx0 + (tx1 - tx0) * vol, cy, k.h * .2f, p); txt(c, "VOLUME", cx, k.y + k.h * .2f, k.w * .5f, k.h * .2f, T.tx, Paint.Align.CENTER); return;
            }
            if (k.act != null && k.act.equals("conn")) {
                p.setColor(link.ok ? 0xFF34D399 : 0xFFFBBF24); c.drawCircle(k.x + k.h * .5f, cy, k.h * .13f, p);
                String md = sp.getString("mode", "wifi"); txt(c, (md.equals("usb") ? "USB" : md.equals("bt") ? "Bluetooth" : "Wi-Fi") + " - " + link.status, k.x + k.h * .8f, cy, k.w - k.h, k.h * .32f, T.tx, Paint.Align.LEFT); return;
            }
            if (k.fnKey) tc = hi ? tc : ac;
            if (k.act != null && k.act.equals("lang")) { txt(c, LANGS[lang], cx, cy, k.w * .8f, k.h * .42f, hi ? tc : ac, Paint.Align.CENTER); return; }
            if (k.l.length() == 0) return;
            if (fn && k.fl != null && !hi) { txt(c, k.fl, cx, cy, k.w * .8f, k.h * .5f, ac, Paint.Align.CENTER); return; }
            String[] fc = face(k); String ml = fc[0], sl = fc[1];
            if (sl != null) { txt(c, sl, cx, k.y + k.h * .33f, k.w * .7f, k.h * .26f, hi ? tc : (T.tx & 0x00FFFFFF) | 0x99000000, Paint.Align.CENTER); txt(c, ml, cx, k.y + k.h * .68f, k.w * .8f, k.h * .34f, tc, Paint.Align.CENTER); }
            else txt(c, ml, cx, cy, k.w * .82f, k.h * (k.kind == 0 ? .4f : .27f), tc, Paint.Align.CENTER);
            if (k.fl != null && !fn) txt(c, k.fl, k.x + k.w - k.w * .12f, k.y + k.h * .8f, k.w * .3f, k.h * .2f, (ac & 0x00FFFFFF) | 0xE0000000, Paint.Align.RIGHT);
            if (k.st == 2) { p.setColor(tc); c.drawRect(cx - k.w * .15f, k.y + k.h * .85f, cx + k.w * .15f, k.y + k.h * .88f, p); }
        }
        @Override protected void onDraw(Canvas c) {
            Theme T = TH[th]; int ac = acc(); p.setStyle(Paint.Style.FILL); p.setShader(bgs); if (bgs == null) p.setColor(T.bg1); c.drawRect(0, 0, getWidth(), getHeight(), p); p.setShader(null);
            for (K k : all) drawK(c, k, T, ac);
            float[] hsv = new float[3]; Color.colorToHSV(ac, hsv);
            for (K k : deck) {
                float m = Math.min(k.w, k.h) * .06f; rf.set(k.x + m, k.y + m, k.x + k.w - m, k.y + k.h - m); float r = rf.height() * .16f;
                int sc = Color.HSVToColor(new float[]{(hsv[0] + k.idx * 34) % 360, .6f, 1f});
                p.setStyle(Paint.Style.FILL); p.setColor(k.on ? sc : T.key); c.drawRoundRect(rf, r, r, p);
                r2.set(rf.left, rf.top, rf.right, rf.top + rf.height() * .13f); p.setColor(sc); c.drawRoundRect(r2, r, r, p);
                if (T.glow) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.4f)); p.setColor((sc & 0x00FFFFFF) | 0xAA000000); c.drawRoundRect(rf, r, r, p); }
                txt(c, dk[k.idx][0], k.x + k.w / 2, k.y + k.h * .58f, k.w * .85f, k.h * .3f, k.on ? (lum(sc) > .6f ? 0xFF101010 : 0xFFFFFFFF) : T.tx, Paint.Align.CENTER);
            }
        }
        String disp(String x) {
            if (x == null) return null;
            if (x.equals("\u200C")) return "ZWNJ";
            if (x.length() == 1 && x.charAt(0) >= 0x064B && x.charAt(0) <= 0x0652) return "\u0640" + x;
            return x;
        }
        String[] ent(K k) { if (lang == 0 || k.sc == 0) return null; return (lang == 1 ? arM : kuM).get(k.sc); }
        boolean shiftOn() { for (K m : mods) if ((m.sc == 0x2A || m.sc == 0x36) && (m.st > 0 || m.on)) return true; return false; }
        boolean hotMods() { for (K m : mods) if ((m.st > 0 || m.on) && (m.sc == 0x1D || m.sc == 0x11D || m.sc == 0x38 || m.sc == 0x138 || m.sc == 0x15B || m.sc == 0x15C)) return true; return false; }
        String[] face(K k) {
            String[] e = ent(k);
            if (e == null) return new String[]{k.l, k.s};
            return shiftOn() ? new String[]{disp(e[1]), disp(e[0])} : new String[]{disp(e[0]), disp(e[1])};
        }
        void cycleLang() { lang = (lang + 1) % 3; sp.edit().putInt("lang", lang).apply(); invalidate(); }
        void buzz() { if (hap) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); }
        void sendKey(int vk, boolean d) {
            link.send("K " + vk + (d ? " d" : " u"));
            if (d && vk == 0xAF) vol = Math.min(1f, vol + .02f); if (d && vk == 0xAE) vol = Math.max(0f, vol - .02f);
        }
        void sendK(K k, boolean d) {
            if (k.fnKey) return;
            int code = (k.useFn && k.fsc != 0) ? k.fsc : k.sc;
            if (code != 0) link.send("S " + code + (d ? " d" : " u")); else if (k.vk != 0) sendKey(k.vk, d);
        }
        void volKey(boolean up) { sendKey(up ? 0xAF : 0xAE, true); sendKey(up ? 0xAF : 0xAE, false); }
        int vkOf(String s) {
            s = s.trim().toLowerCase();
            switch (s) {
                case "ctrl": return 0xA2; case "shift": return 0xA0; case "alt": return 0xA4; case "win": return 0x5B; case "esc": return 0x1B; case "tab": return 9;
                case "enter": return 13; case "space": return 0x20; case "del": return 0x2E; case "ins": return 0x2D; case "home": return 0x24; case "end": return 0x23;
                case "pgup": return 0x21; case "pgdn": return 0x22; case "up": return 0x26; case "down": return 0x28; case "left": return 0x25; case "right": return 0x27;
                case "backspace": return 8; case "prtsc": return 0x2C; case "mute": return 0xAD; case "volup": return 0xAF; case "voldown": return 0xAE;
                case "play": return 0xB3; case "next": return 0xB0; case "prev": return 0xB1; default: break;
            }
            if (s.length() == 1) { char ch = s.charAt(0); if (ch >= 'a' && ch <= 'z') return ch - 32; if (ch >= '0' && ch <= '9') return ch; }
            if (s.matches("f([1-9]|1[0-2])")) return 0x70 + Integer.parseInt(s.substring(1)) - 1;
            return 0;
        }
        void runDeck(String[] d) {
            if (d[1].equals("key")) {
                List<Integer> v = new ArrayList<Integer>(); for (String s : d[2].split("\\+")) { int k = vkOf(s); if (k > 0) v.add(k); }
                for (int k : v) sendKey(k, true); for (int i = v.size() - 1; i >= 0; i--) sendKey(v.get(i), false);
            } else if (d[1].equals("run")) link.send("X " + d[2].replace("\n", " "));
            else link.send("T " + d[2].replace("\n", " "));
        }
        void releaseLatched() { for (K k : mods) if (k.st == 1) { sendK(k, false); k.st = 0; } }
        void startRep(final int id, final K k) {
            Runnable r = new Runnable() { public void run() { if (k.on && act.get(id) == k) { if (k.uni != null) link.send("T " + k.uni); else sendK(k, true); h.postDelayed(this, 38); } } };
            reps.put(id, r); h.postDelayed(r, 420);
        }
        boolean in(float a, float b, float w, float hh, float x, float y) { return x >= a && x < a + w && y >= b && y < b + hh; }
        boolean inK(K k, float x, float y) { return in(k.x, k.y, k.w, k.h, x, y) || (k.w2 > 0 && in(k.x2, k.y2, k.w2, k.h2, x, y)); }
        K hit(float x, float y) {
            for (K k : all) if (k.kind >= 0 && inK(k, x, y)) return k;
            for (K k : deck) if (inK(k, x, y)) return k;
            return null;
        }
        void down(final int id, float x, float y) {
            K k = hit(x, y); if (k == null) return; act.put(id, k); k.on = true; long now = SystemClock.uptimeMillis(); buzz();
            if (k.kind == 0 || k.kind == 2 || k.kind == 7) {
                k.useFn = fnOn(); String[] e = ent(k); k.uni = null;
                if (e != null && !hotMods() && !(k.useFn && k.fsc != 0)) { k.uni = shiftOn() ? e[1] : e[0]; link.send("T " + k.uni); } else sendK(k, true);
                if (k.kind != 2 || k.rep) startRep(id, k);
            }
            else if (k.kind == 1) {
                if (k.st == 0) { sendK(k, true); k.pend = 1; k.t0 = now; } else if (k.st == 1) k.pend = (now - k.t1 < 450) ? 2 : 3; else k.pend = 3;
            } else if (k.kind == 5) lx.put(id, x);
            else if (k.kind == 4) {
                final int i = k.idx; Runnable r = new Runnable() { public void run() { fired.add(id); editDeck(i); } }; reps.put(id, r); h.postDelayed(r, 550);
            }
        }
        void move(int id, float x, float y) {
            K k = act.get(id); if (k == null) return;
            if (k.kind == 5) {
                float d = x - lx.get(id); lx.put(id, x); sacc += d; float st = Math.max(4f, slider.w / 50f);
                while (sacc >= st) { volKey(true); sacc -= st; } while (sacc <= -st) { volKey(false); sacc += st; }
            } else if ((k.kind == 0 || k.kind == 2 || k.kind == 7) && !inK(k, x, y)) up(id);
        }
        void up(int id) {
            K k = act.remove(id); if (k == null) return; k.on = false; Runnable r = reps.remove(id); if (r != null) h.removeCallbacks(r); long now = SystemClock.uptimeMillis();
            if (k.kind == 0 || k.kind == 2 || k.kind == 7) { if (k.uni == null) sendK(k, false); k.uni = null; if (k.kind == 0) releaseLatched(); }
            else if (k.kind == 1) {
                if (k.pend == 1) { if (now - k.t0 >= 300) { sendK(k, false); k.st = 0; } else { k.st = 1; k.t1 = now; } }
                else if (k.pend == 2) k.st = 2; else if (k.pend == 3) { sendK(k, false); k.st = 0; }
                k.pend = 0;
            } else if (k.kind == 3) { if ("conn".equals(k.act)) connDialog(); else if ("lang".equals(k.act)) cycleLang(); else themeDialog(); }
            else if (k.kind == 4) { if (!fired.remove(id)) runDeck(dk[k.idx]); }
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            int a = e.getActionMasked(), ix = e.getActionIndex();
            switch (a) {
                case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN: down(e.getPointerId(ix), e.getX(ix), e.getY(ix)); break;
                case MotionEvent.ACTION_MOVE: for (int i = 0; i < e.getPointerCount(); i++) move(e.getPointerId(i), e.getX(i), e.getY(i)); break;
                case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP: up(e.getPointerId(ix)); break;
                case MotionEvent.ACTION_CANCEL: for (Integer id : new ArrayList<Integer>(act.keySet())) up(id); break;
                default: break;
            }
            invalidate(); return true;
        }
    }
}
