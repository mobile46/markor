/*
 * Copyright (c) 2014 Jeff Martin
 * Copyright (c) 2015 Pedro Lafuente
 * Copyright (c) 2017-2018 Gregor Santner
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package other.writeily.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.gsantner.markor.R;
import net.gsantner.markor.format.markdown.MarkdownTextConverter;
import net.gsantner.markor.ui.FilesystemDialogCreator;
import net.gsantner.markor.util.AppSettings;
import net.gsantner.markor.util.ContextUtils;
import net.gsantner.markor.util.DocumentIO;
import net.gsantner.opoc.ui.FilesystemDialogAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import other.writeily.activity.FilesystemListFragment;

public class FilesWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context _context;
    private File[] _widgetFilesList = new File[0];
    private int _appWidgetId;
    private File _dir;

    public FilesWidgetFactory(Context context, Intent intent) {
        _context = context;
        _appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        _dir = (File) intent.getSerializableExtra(DocumentIO.EXTRA_PATH);
    }

    @Override
    public void onCreate() {
        onDataSetChanged();
    }

    @Override
    public void onDataSetChanged() {
        updateFiles();
    }

    private void updateFiles() {
        _widgetFilesList = (_dir == null) ? new File[0] : _dir.listFiles(file ->
                !file.isDirectory() && ContextUtils.get().isMaybeMarkdownFile(file)
        );
        if (_dir != null && _dir.equals(FilesystemDialogAdapter.VIRTUAL_STORAGE_RECENTS)) {
            _widgetFilesList = FilesystemDialogCreator.strlistToArray(AppSettings.get().getRecentDocuments());
        }
        if (_dir != null && _dir.equals(FilesystemDialogAdapter.VIRTUAL_STORAGE_POPULAR)) {
            _widgetFilesList = FilesystemDialogCreator.strlistToArray(AppSettings.get().getPopularDocuments());
        }
        ArrayList<File> files = new ArrayList<>(Arrays.asList(_widgetFilesList));

        //noinspection StatementWithEmptyBody
        if (_dir != null && (_dir.equals(FilesystemDialogAdapter.VIRTUAL_STORAGE_RECENTS) || _dir.equals(FilesystemDialogAdapter.VIRTUAL_STORAGE_POPULAR))) {
            // nothing to do
        } else {
            FilesystemListFragment.sortFolder(files);
        }
        _widgetFilesList = files.toArray(new File[files.size()]);
    }

    @Override
    public void onDestroy() {
        _widgetFilesList = new File[0];
    }

    @Override
    public int getCount() {
        return _widgetFilesList == null ? 0 : _widgetFilesList.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rowView = new RemoteViews(_context.getPackageName(), R.layout.widget_file_item);
        rowView.setTextViewText(R.id.widget_note_title, "???");
        if (position < _widgetFilesList.length) {
            File file = _widgetFilesList[position];
            Intent fillInIntent = new Intent().putExtra(DocumentIO.EXTRA_PATH, file).putExtra(DocumentIO.EXTRA_PATH_IS_FOLDER, file.isDirectory());
            rowView.setTextViewText(R.id.widget_note_title, MarkdownTextConverter.MD_EXTENSION_PATTERN.matcher(file.getName()).replaceAll(""));
            rowView.setOnClickFillInIntent(R.id.widget_note_title, fillInIntent);
        }
        return rowView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
