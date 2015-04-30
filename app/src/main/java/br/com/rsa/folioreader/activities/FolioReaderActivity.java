package br.com.rsa.folioreader.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.adapters.FolioReaderIndexAdapter;
import br.com.rsa.folioreader.adapters.FolioReaderPagerAdapter;
import br.com.rsa.folioreader.configuration.Configuration;
import br.com.rsa.folioreader.entities.BookDecompressed;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;


public class FolioReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        menuDrawer.setContentView(R.layout.activity_folio_reader);
        menuDrawer.setMenuView(R.layout.folioreader_menudrawer_items);
        menuDrawer.setDropShadowColor(getResources().getColor(R.color.shadowIndexMenuAndSeparator));
        menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
        init();

        if (savedInstanceState != null) {
            activeViewId = savedInstanceState.getInt(STATE_ACTIVE_VIEW_ID);
        }
        viewPager.setAdapter(new FolioReaderPagerAdapter(getSupportFragmentManager(), bookDecompressed.getUrlResources()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folio_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        menuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, menuDrawer.saveState());
        outState.putInt(STATE_ACTIVE_VIEW_ID, activeViewId);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = menuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            menuDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }

    private void init() {
        bookDecompressed = (BookDecompressed) Configuration.getData("key-book");
        viewPager = (VerticalViewPager) findViewById(R.id.folioreader_vertical_viewpager);
        listViewIndex = (ListView) findViewById(R.id.folioreader_listview_index);
        new CreateIndex().execute(bookDecompressed.getBook().getTableOfContents());
    }

    private VerticalViewPager viewPager;
    private MenuDrawer menuDrawer;
    private static final String STATE_ACTIVE_VIEW_ID = "net.simonvt.menudrawer.samples.WindowSample.activeViewId";
    private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.WindowSample.menuDrawer";
    private int activeViewId;
    private ListView listViewIndex;
    private BookDecompressed bookDecompressed;

    /****** Class AsyncTask ******/
    private class CreateIndex extends AsyncTask<TableOfContents, Void, List<TOCReference>> {

        private List<TOCReference> list;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list = new ArrayList<>();
        }

        @Override
        protected List<TOCReference> doInBackground(TableOfContents... tableOfContentses) {
            for (TOCReference t : tableOfContentses[0].getTocReferences()) {
                getRecursive(t);
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<TOCReference> tocReferences) {
            super.onPostExecute(tocReferences);
            listViewIndex.setAdapter(new FolioReaderIndexAdapter(getApplicationContext(), tocReferences));
            listViewIndex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    viewPager.setCurrentItem(position);
                    menuDrawer.closeMenu();
                }
            });
        }

        private void getRecursive(TOCReference tocReference){
            list.add(tocReference);

            for (TOCReference t : tocReference.getChildren()) {
                getRecursive(t);
            }
        }
    }
}
