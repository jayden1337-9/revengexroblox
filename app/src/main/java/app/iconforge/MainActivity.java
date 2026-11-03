package app.iconforge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.*;

public class MainActivity extends Activity {
    private final int ink=Color.rgb(28,30,36), secondary=Color.rgb(112,116,126);
    private final int glass=Color.argb(220,255,255,255), accent=Color.rgb(70,112,245);
    private final List<AppEntry> apps=new ArrayList<>();
    private final Map<String,String> names=new HashMap<>();
    private final Map<String,Uri> icons=new HashMap<>();
    private SharedPreferences prefs;
    private LinearLayout root, content, topBar;
    private EditText search;
    private boolean library=false;
    private AppAdapter adapter;

    static class AppEntry {
        String label,pkg; Drawable icon;
        AppEntry(String l,String p,Drawable i){label=l;pkg=p;icon=i;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("customizations",MODE_PRIVATE);
        loadSaved();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.rgb(235,238,246));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        loadApps();
        showHome();
    }
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,float radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;
    }
    private TextView label(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private void loadSaved(){
        Map<String,?> all=prefs.getAll();
        for(String k:all.keySet())if(k.startsWith("name:"))names.put(k.substring(5),String.valueOf(all.get(k)));
        for(String k:all.keySet())if(k.startsWith("icon:"))try{icons.put(k.substring(5),Uri.parse(String.valueOf(all.get(k))));}catch(Exception ignored){}
    }
    private String shown(AppEntry a){return names.containsKey(a.pkg)?names.get(a.pkg):a.label;}
    private void loadApps(){
        apps.clear();PackageManager pm=getPackageManager();
        Intent q=new Intent(Intent.ACTION_MAIN,null);q.addCategory(Intent.CATEGORY_LAUNCHER);
        Set<String> seen=new HashSet<>();
        for(android.content.pm.ResolveInfo r:pm.queryIntentActivities(q,0)){
            if(r.activityInfo==null)continue;String p=r.activityInfo.packageName;
            if(p.equals(getPackageName())||!seen.add(p))continue;
            apps.add(new AppEntry(r.loadLabel(pm).toString(),p,r.loadIcon(pm)));
        }
        Collections.sort(apps,Comparator.comparing(a->shown(a).toLowerCase(Locale.ROOT)));
    }
    private void base(String title,String subtitle){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22),dp(38),dp(22),dp(12));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(218,229,255),Color.rgb(239,232,250),Color.rgb(246,247,251)}));
        topBar=new LinearLayout(this);topBar.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading=label(title,30,ink,true);
        topBar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView dots=label("•••",22,secondary,true);dots.setPadding(dp(12),dp(4),dp(4),dp(4));topBar.addView(dots);
        root.addView(topBar);
        TextView sub=label(subtitle,14,secondary,false);
        LinearLayout.LayoutParams sm=new LinearLayout.LayoutParams(-1,-2);sm.topMargin=dp(3);sm.bottomMargin=dp(18);root.addView(sub,sm);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private void showHome(){
        library=false;base("IconForge","Tuesday · Make this space yours");
        TextView date=label("Your space",17,ink,true);
        content.addView(date);
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(0,dp(16),0,dp(12));content.addView(grid);
        int cols=4;LinearLayout line=null;int count=0;
        for(AppEntry a:apps){
            if(count%cols==0){line=new LinearLayout(this);line.setGravity(Gravity.TOP);grid.addView(line,new LinearLayout.LayoutParams(-1,dp(105)));}
            line.addView(iconTile(a),new LinearLayout.LayoutParams(0,-2,1));count++;
            if(count>=16)break;
        }
        View spacer=new View(this);content.addView(spacer,new LinearLayout.LayoutParams(1,0,1));
        LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER);dock.setPadding(dp(8),dp(10),dp(8),dp(10));
        dock.setBackground(shape(Color.argb(155,255,255,255),28));
        for(int i=0;i<Math.min(4,apps.size());i++)dock.addView(dockIcon(apps.get(i)),new LinearLayout.LayoutParams(0,dp(58),1));
        LinearLayout.LayoutParams dockp=new LinearLayout.LayoutParams(-1,dp(76));dockp.bottomMargin=dp(12);content.addView(dock,dockp);
        TextView open=label("⌕     App Library",16,ink,true);open.setGravity(Gravity.CENTER);
        open.setBackground(shape(glass,22));open.setPadding(dp(12),dp(14),dp(12),dp(14));
        open.setOnClickListener(v->showLibrary());content.addView(open);
    }
    private View iconTile(AppEntry a){
        LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);
        ImageView im=new ImageView(this);im.setImageDrawable(getIcon(a));im.setScaleType(ImageView.ScaleType.FIT_CENTER);
        im.setBackground(shape(Color.WHITE,19));im.setElevation(dp(4));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(58),dp(58));tile.addView(im,ip);
        TextView name=label(shown(a),11,ink,false);name.setGravity(Gravity.CENTER);name.setSingleLine(true);
        name.setMaxEms(11);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(6);tile.addView(name,np);
        tile.setOnClickListener(v->launch(a));tile.setOnLongClickListener(v->{edit(a);return true;});return tile;
    }
    private View dockIcon(AppEntry a){
        ImageView im=new ImageView(this);im.setImageDrawable(getIcon(a));im.setScaleType(ImageView.ScaleType.FIT_CENTER);
        im.setPadding(dp(5),dp(5),dp(5),dp(5));im.setOnClickListener(v->launch(a));
        im.setOnLongClickListener(v->{edit(a);return true;});return im;
    }
    private Drawable getIcon(AppEntry a){
        Uri u=icons.get(a.pkg);if(u!=null)try{return Drawable.createFromStream(getContentResolver().openInputStream(u),null);}catch(Exception ignored){}
        return a.icon;
    }
    private void showLibrary(){
        library=true;base("App Library","Search and find everything");
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);search.setTextColor(ink);
        search.setHintTextColor(secondary);search.setHint("⌕   Search apps");search.setPadding(dp(16),0,dp(16),0);
        search.setBackground(shape(glass,18));content.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView sc=new ScrollView(this);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,0,1);sp.topMargin=dp(14);content.addView(sc,sp);
        ListViewlessList list=new ListViewlessList();adapter=new AppAdapter();list.setAdapter(adapter);adapter.listRef=list;list.refresh();sc.addView(list);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){adapter.filter(s.toString());}public void afterTextChanged(Editable e){}});
        TextView home=label("‹   Home Screen",15,accent,true);home.setGravity(Gravity.CENTER);home.setPadding(0,dp(15),0,dp(8));home.setOnClickListener(v->showHome());content.addView(home);
    }
    private class ListViewlessList extends LinearLayout{
        BaseAdapter a;ListViewlessList(){super(MainActivity.this);setOrientation(VERTICAL);}
        void setAdapter(BaseAdapter x){a=x;refresh();}
        void refresh(){removeAllViews();if(a==null)return;for(int i=0;i<a.getCount();i++)addView(a.getView(i,null,this));}
    }
    private class AppAdapter extends BaseAdapter{
        final List<AppEntry> items=new ArrayList<>();
        AppAdapter(){filter("");}
        void filter(String q){items.clear();String n=q.toLowerCase(Locale.ROOT);for(AppEntry a:apps)if(shown(a).toLowerCase(Locale.ROOT).contains(n))items.add(a);Collections.sort(items,Comparator.comparing(a->shown(a).toLowerCase(Locale.ROOT)));notifyDataSetChanged();if(listRef!=null)listRef.refresh();}
        ListViewlessList listRef;
        public int getCount(){return items.size();}public Object getItem(int p){return items.get(p);}public long getItemId(int p){return p;}
        public View getView(int p,View old,ViewGroup parent){
            AppEntry a=items.get(p);LinearLayout row=new LinearLayout(MainActivity.this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(9),dp(12),dp(9));row.setBackground(shape(glass,18));
            ImageView im=new ImageView(MainActivity.this);im.setImageDrawable(getIcon(a));row.addView(im,new LinearLayout.LayoutParams(dp(42),dp(42)));
            TextView name=label(shown(a),15,ink,true);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,-2,1);np.leftMargin=dp(14);row.addView(name,np);
            TextView more=label("···",20,secondary,true);row.addView(more);
            row.setOnClickListener(v->launch(a));row.setOnLongClickListener(v->{edit(a);return true;});
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(7);row.setLayoutParams(rp);return row;
        }
    }
    private void launch(AppEntry a){Intent i=getPackageManager().getLaunchIntentForPackage(a.pkg);if(i!=null)startActivity(i);else Toast.makeText(this,"Unable to open app",Toast.LENGTH_SHORT).show();}
    private void edit(AppEntry a){
        EditText input=new EditText(this);input.setSingleLine(true);input.setText(shown(a));
        new AlertDialog.Builder(this).setTitle("Customize app").setMessage("Change the name shown in IconForge, or choose a new icon.")
        .setView(input).setPositiveButton("Save name",(d,w)->{String s=input.getText().toString().trim();if(!s.isEmpty()){names.put(a.pkg,s);prefs.edit().putString("name:"+a.pkg,s).apply();loadApps();if(library)showLibrary();else showHome();}})
        .setNeutralButton("Change icon",(d,w)->pickIcon(a)).setNegativeButton("Cancel",null).show();
    }
    private void pickIcon(AppEntry a){pending=a.pkg;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,71);}
    private String pending;
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==71&&c==RESULT_OK&&d!=null&&pending!=null){Uri u=d.getData();getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);icons.put(pending,u);prefs.edit().putString("icon:"+pending,u.toString()).apply();if(library)showLibrary();else showHome();}}
    @Override public void onBackPressed(){if(library)showHome();else super.onBackPressed();}
}
