package com.pestopasta.slidingmenu.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pestopasta.cluzcs160.R;
import com.pestopasta.slidingmenu.model.NavDrawerItem;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    int layoutResID;

    public NavDrawerListAdapter(Context context, int layoutResID, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
        this.layoutResID = layoutResID;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.itemName = (TextView) view.findViewById(R.id.drawer_itemName);
            drawerHolder.checkedItemName = (TextView) view.findViewById(R.id.drawer_checkedItemName);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);
            drawerHolder.editText = (EditText) view.findViewById(R.id.drawer_editText);
            drawerHolder.checkBox = (CheckBox) view.findViewById(R.id.drawer_checkbox);

            drawerHolder.title = (TextView) view.findViewById(R.id.drawerTitle);

            drawerHolder.headerLayout = (LinearLayout) view
                    .findViewById(R.id.headerLayout);
            drawerHolder.itemLayout = (LinearLayout) view
                    .findViewById(R.id.itemLayout);
            drawerHolder.userImageLayout = (LinearLayout) view
                    .findViewById(R.id.userImageLayout);
            drawerHolder.editTextLayout = (LinearLayout) view
                    .findViewById(R.id.editTextLayout);
            drawerHolder.checkBoxLayout = (LinearLayout) view
                    .findViewById(R.id.checkboxLayout);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        NavDrawerItem dItem = (NavDrawerItem) this.navDrawerItems.get(position);

        if (dItem.isUserImage()) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkBoxLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.editTextLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.userImageLayout.setVisibility(LinearLayout.VISIBLE);

            //Load user pic from database
            //drawerHolder.userImage.setImageDrawable()
        } else if (dItem.isEditText()) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkBoxLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.editTextLayout.setVisibility(LinearLayout.VISIBLE);
            drawerHolder.userImageLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.editText.setHint(dItem.getHelpText());

            //Load user pic from database
            //drawerHolder.userImage.setImageDrawable()
        } else if (dItem.isCheckBox()) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.userImageLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkBoxLayout.setVisibility(LinearLayout.VISIBLE);
            drawerHolder.editTextLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkedItemName.setText(dItem.getItemName());

            drawerHolder.checkBox.setFocusableInTouchMode(false);
            drawerHolder.checkBox.setFocusable(false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawerHolder.checkBox.setChecked(drawerHolder.checkBox.isChecked());
                }
            });



            //Load user pic from database
            //drawerHolder.userImage.setImageDrawable()
        }
        else if (dItem.getTitle() != null) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.VISIBLE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.userImageLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkBoxLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.editTextLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.title.setText(dItem.getTitle());
            drawerHolder.headerLayout.setFocusableInTouchMode(false);
            drawerHolder.headerLayout.setFocusable(false);
            drawerHolder.headerLayout.setClickable(false);

        } else {
            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);
            drawerHolder.userImageLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.checkBoxLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.editTextLayout.setVisibility(LinearLayout.GONE);

            if (dItem.getIcon() >= 0) {
                drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(
                        dItem.getIcon()));
            }
            drawerHolder.itemName.setText(dItem.getItemName());
        }
        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 2) {
            return false;
        } else {
            return super.isEnabled(position);
        }
    }

    private static class DrawerItemHolder {
        TextView itemName, title, checkedItemName;
        EditText editText;
        ImageView icon, userImage;
        CheckBox checkBox;

        LinearLayout headerLayout, itemLayout, userImageLayout, checkBoxLayout, editTextLayout;
    }
}
