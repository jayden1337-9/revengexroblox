package app.iconforge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(15, 17, 23);
    private final int panel = Color.rgb(25, 28, 37);
    private final int fg = Color.rgb(240, 242, 248);
    private final int muted = Color.rgb(155, 162, 178);
    private final List<AppEntry> allApps = new ArrayList<>();
    private final Map<String, String> renamed = new HashMap<>();
    private final Map<String, Bitmap> customIcons = new HashMap<>();
    private AppAdapter adapter;
    private EditText search;
    private String pendingIconPackage;

    static class AppEntry {
        String label, packageName;
        Drawable icon;
        AppEntry(String l, String p, Drawable i) { label=l; packageName=p; icon=i; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildUi();
        loadApps();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(8));
        root.setBackgroundColor(bg);

        TextView title = text("IconForge", 28, fg);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = text("Your apps, your look.", 13, muted);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.bottomMargin = dp(18);
        root.addView(subtitle, sp);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(fg);
        search.setHintTextColor(muted);
        search.setHint("Search apps");
        search.setPadding(dp(14), 0, dp(14), 0);
        search.setBackgroundTintList(android.content.res.ColorStateList.valueOf(muted));
        root.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        ListView list = new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(dp(5));
        list.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1);
        lp.topMargin = dp(12);
        root.addView(list, lp);

        adapter = new AppAdapter();
        list.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { adapter.filter(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        list.setOnItemClickListener((parent, view, position, id) -> launch(adapter.items.get(position)));
        list.setOnItemLongClickListener((parent, view, position, id) -> { editApp(adapter.items.get(position)); return true; });
        setContentView(root);
    }

    private void loadApps() {
        allApps.clear();
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        for (android.content.pm.ResolveInfo ri : pm.queryIntentActivities(query, 0)) {
            if (ri.activityInfo == null || ri.activityInfo.packageName.equals(getPackageName())) continue;
            String pkg = ri.activityInfo.packageName;
            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            boolean exists = false;
            for (AppEntry e : allApps) if (e.packageName.equals(pkg)) { exists=true; break; }
            if (!exists) allApps.add(new AppEntry(label, pkg, icon));
        }
        Collections.sort(allApps, Comparator.comparing(a -> displayName(a).toLowerCase(Locale.ROOT)));
        adapter.set(allApps);
    }

    private String displayName(AppEntry e) { return renamed.containsKey(e.packageName) ? renamed.get(e.packageName) : e.label; }

    private void launch(AppEntry e) {
        Intent i = getPackageManager().getLaunchIntentForPackage(e.packageName);
        if (i != null) startActivity(i);
        else Toast.makeText(this, "Can't open this app", Toast.LENGTH_SHORT).show();
    }

    private void editApp(AppEntry e) {
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setText(displayName(e));
        new AlertDialog.Builder(this)
            .setTitle("Edit " + e.label)
            .setMessage("Change the name shown inside IconForge. Long-press again to edit its icon.")
            .setView(name)
            .setPositiveButton("Save name", (d,w) -> {
                String value = name.getText().toString().trim();
                if (!value.isEmpty()) renamed.put(e.packageName, value);
                adapter.filter(search.getText().toString());
            })
            .setNeutralButton("Choose icon", (d,w) -> chooseIcon(e))
            .setNegativeButton("Cancel", null).show();
    }

    private void chooseIcon(AppEntry e) {
        pendingIconPackage = e.packageName;
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("image/*");
        startActivityForResult(pick, 42);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == RESULT_OK && data != null && pendingIconPackage != null) {
            try (InputStream in = getContentResolver().openInputStream(data.getData())) {
                Bitmap b = BitmapFactory.decodeStream(in);
                if (b != null) customIcons.put(pendingIconPackage, b);
                adapter.notifyDataSetChanged();
            } catch (Exception ex) {
                Toast.makeText(this, "Couldn't load that image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AppAdapter extends BaseAdapter {
        final List<AppEntry> items = new ArrayList<>();
        void set(List<AppEntry> source) { items.clear(); items.addAll(source); notifyDataSetChanged(); }
        void filter(String q) {
            items.clear();
            String needle=q.toLowerCase(Locale.ROOT);
            for (AppEntry e: allApps) if (displayName(e).toLowerCase(Locale.ROOT).contains(needle)) items.add(e);
            Collections.sort(items, Comparator.comparing(a -> displayName(a).toLowerCase(Locale.ROOT)));
            notifyDataSetChanged();
        }
        public int getCount(){return items.size();}
        public Object getItem(int p){return items.get(p);}
        public long getItemId(int p){return p;}
        public View getView(int p, View convert, ViewGroup parent) {
            AppEntry e=items.get(p);
            LinearLayout row=new LinearLayout(MainActivity.this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12),dp(8),dp(12),dp(8));
            row.setBackgroundColor(panel);
            ImageView image=new ImageView(MainActivity.this);
            Bitmap custom=customIcons.get(e.packageName);
            if(custom!=null) image.setImageBitmap(custom); else image.setImageDrawable(e.icon);
            row.addView(image,new LinearLayout.LayoutParams(dp(42),dp(42)));
            LinearLayout labels=new LinearLayout(MainActivity.this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setPadding(dp(14),0,0,0);
            TextView name=text(displayName(e),15,fg);
            TextView pkg=text(e.packageName,11,muted);
            labels.addView(name); labels.addView(pkg);
            row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
            TextView letter=text(displayName(e).substring(0,1).toUpperCase(Locale.ROOT),12,muted);
            row.addView(letter);
            return row;
        }
    }
}
