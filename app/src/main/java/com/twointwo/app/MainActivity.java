
package com.twointwo.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class MainActivity extends Activity {

    LinearLayout root, content, header;
    SharedPreferences prefs;
    JSONArray records = new JSONArray();
    String page = "Home";
    boolean dark = false;
    String[] currencies = {"INR (₹)", "USD ($)"};

    int bg, fg, card, muted;
    final DecimalFormat numberFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("twoinone_data", MODE_PRIVATE);
        dark = prefs.getBoolean("dark", false);
        loadData();
        showApp();
    }

    void loadData() {
        try {
            records = new JSONArray(prefs.getString("records", "[]"));
        } catch (Exception e) {
            records = new JSONArray();
        }
    }

    void saveData() {
        prefs.edit().putString("records", records.toString()).apply();
    }

    void colors() {
        bg = dark ? Color.rgb(18,18,18) : Color.rgb(247,247,247);
        fg = dark ? Color.WHITE : Color.rgb(25,25,25);
        card = dark ? Color.rgb(38,38,38) : Color.WHITE;
        muted = dark ? Color.LTGRAY : Color.DKGRAY;
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(fg);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    void showApp() {
        colors();

        root = column();
        root.setBackgroundColor(bg);

        header = column();
        header.setPadding(20, 18, 20, 12);
        header.addView(text("2in1", 28, true));
        header.addView(text("Money & Notes", 14, false));
        root.addView(header);

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = row();
        nav.setPadding(8, 4, 8, 8);

        String[] pages = {"Home", "Money", "Savings", "Notes", "Settings"};
        for (String p : pages) {
            Button b = new Button(this);
            b.setText(p);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setTextColor(fg);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                page.equals(p) ? (dark ? Color.WHITE : Color.BLACK) : card));
            if (page.equals(p)) {
                b.setTextColor(dark ? Color.BLACK : Color.WHITE);
            }
            LinearLayout.LayoutParams bp = lp(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            bp.setMargins(3, 0, 3, 0);
            nav.addView(b, bp);
            b.setOnClickListener(v -> {
                page = p;
                showApp();
            });
        }
        navScroll.addView(nav);
        root.addView(navScroll);

        ScrollView scroll = new ScrollView(this);
        content = column();
        content.setPadding(18, 10, 18, 24);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        if (page.equals("Home")) showHome();
        else if (page.equals("Money")) showMoney();
        else if (page.equals("Savings")) showSavings();
        else if (page.equals("Notes")) showNotes();
        else showSettings();

        setContentView(root);
    }

    void addCard(View v) {
        v.setBackground(shape(card, 22));
        v.setPadding(16, 14, 16, 14);
        LinearLayout.LayoutParams p = lp(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, 12);
        content.addView(v, p);
    }

    double total(String type, String currency) {
        double sum = 0;
        for (int i = 0; i < records.length(); i++) {
            JSONObject o = records.optJSONObject(i);
            if (o == null) continue;
            if (type.equals(o.optString("type")) &&
                currency.equals(o.optString("currency"))) {
                sum += o.optDouble("amount", 0);
            }
        }
        return sum;
    }

    String money(double amount, String currency) {
        return (currency.equals("USD") ? "$" : "₹") +
            numberFormat.format(amount);
    }

    void showHome() {
        content.addView(text("Your overview", 23, true));
        content.addView(text("Your money, your way.", 14, false));

        for (String c : new String[]{"INR", "USD"}) {
            LinearLayout box = column();
            box.addView(text(c.equals("INR") ?
                "Indian Rupee (INR)" : "US Dollar (USD)", 17, true));
            box.addView(text("Income: " +
                money(total("Income", c), c), 16, false));
            box.addView(text("Expenses: " +
                money(total("Expense", c), c), 16, false));
            box.addView(text("Savings: " +
                money(total("Saving", c), c), 16, false));
            addCard(box);
        }

        Button income = makeButton("+ Add Income");
        Button expense = makeButton("+ Add Expense");
        Button saving = makeButton("+ Add Saving");
        Button note = makeButton("+ New Note");

        income.setOnClickListener(v -> openEditor("Income", -1));
        expense.setOnClickListener(v -> openEditor("Expense", -1));
        saving.setOnClickListener(v -> openEditor("Saving", -1));
        note.setOnClickListener(v -> openEditor("Note", -1));

        addCard(income);
        addCard(expense);
        addCard(saving);
        addCard(note);
    }

    Button makeButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(fg);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(card));
        return b;
    }

    void showMoney() {
        content.addView(text("Income & Expenses", 23, true));
        content.addView(text(
            "Add details, notes and currency for each entry.", 14, false));

        Button income = makeButton("+ Add Income");
        income.setOnClickListener(v -> openEditor("Income", -1));
        addCard(income);

        Button expense = makeButton("+ Add Expense");
        expense.setOnClickListener(v -> openEditor("Expense", -1));
        addCard(expense);

        showRecords("Income");
        showRecords("Expense");
    }

    void showSavings() {
        content.addView(text("Savings", 23, true));
        content.addView(text("Track your saved money.", 14, false));

        Button add = makeButton("+ Add Saving");
        add.setOnClickListener(v -> openEditor("Saving", -1));
        addCard(add);
        showRecords("Saving");
    }

    void showNotes() {
        content.addView(text("My Notes", 23, true));
        content.addView(text("Write and keep your notes.", 14, false));

        Button add = makeButton("+ New Note");
        add.setOnClickListener(v -> openEditor("Note", -1));
        addCard(add);
        showRecords("Note");
    }

    void showRecords(String type) {
        boolean found = false;
        for (int i = records.length() - 1; i >= 0; i--) {
            JSONObject o = records.optJSONObject(i);
            if (o == null || !type.equals(o.optString("type"))) continue;
            found = true;

            final int index = i;
            LinearLayout box = column();
            box.addView(text(o.optString("title"), 18, true));

            if (!type.equals("Note")) {
                box.addView(text(money(
                    o.optDouble("amount", 0),
                    o.optString("currency", "INR")), 16, false));
            }

            String notes = o.optString("notes", "");
            if (!notes.isEmpty()) {
                TextView n = text(notes, 14, false);
                n.setPadding(0, 5, 0, 8);
                box.addView(n);
            }

            LinearLayout actions = row();
            Button edit = makeButton("Edit");
            Button delete = makeButton("Delete");

            edit.setOnClickListener(v -> openEditor(type, index));
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete entry?")
                .setMessage("This entry will be removed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    JSONArray updated = new JSONArray();
                    for (int j = 0; j < records.length(); j++) {
                        if (j != index) updated.put(records.opt(j));
                    }
                    records = updated;
                    saveData();
                    showApp();
                }).show());

            actions.addView(edit, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            actions.addView(delete, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            box.addView(actions);
            addCard(box);
        }

        if (!found) {
            addCard(text("No " + type.toLowerCase() +
                " entries yet.", 15, false));
        }
    }

    void openEditor(String type, int index) {
        JSONObject old = index >= 0 ? records.optJSONObject(index) : null;

        LinearLayout form = column();
        form.setPadding(20, 8, 20, 0);

        EditText title = new EditText(this);
        title.setSingleLine(true);
        title.setHint(type.equals("Note") ? "Note title" : type + " name");
        if (old != null) title.setText(old.optString("title"));
        form.addView(title);

        EditText amount = new EditText(this);
        amount.setSingleLine(true);
        amount.setHint("Amount");
        amount.setInputType(8194);
        if (old != null && !type.equals("Note")) {
            amount.setText(String.valueOf(old.optDouble("amount", 0)));
        }
        if (type.equals("Note")) amount.setVisibility(View.GONE);
        form.addView(amount);

        Spinner currency = new Spinner(this);
        currency.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, currencies));
        if (old != null && old.optString("currency", "INR").equals("USD")) {
            currency.setSelection(1);
        }
        if (type.equals("Note")) currency.setVisibility(View.GONE);
        form.addView(currency);

        EditText notes = new EditText(this);
        notes.setHint(type.equals("Note") ?
            "Write your note here" : "Notes / description (optional)");
        notes.setMinLines(2);
        notes.setGravity(Gravity.TOP);
        if (old != null) notes.setText(old.optString("notes"));
        form.addView(notes);

        new AlertDialog.Builder(this)
            .setTitle((index >= 0 ? "Edit " : "Add ") + type)
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", (dialog, which) -> {
                String name = title.getText().toString().trim();
                String noteText = notes.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "Enter a name or title",
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                double value = 0;
                if (!type.equals("Note")) {
                    try {
                        value = Double.parseDouble(
                            amount.getText().toString().trim());
                        if (value < 0) throw new NumberFormatException();
                    } catch (Exception e) {
                        Toast.makeText(this, "Enter a valid amount",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                JSONObject item = new JSONObject();
                try {
                    item.put("type", type);
                    item.put("title", name);
                    item.put("amount", value);
                    item.put("currency", currency.getSelectedItemPosition()
                        == 1 ? "USD" : "INR");
                    item.put("notes", noteText);

                    if (index >= 0) records.put(index, item);
                    else records.put(item);

                    saveData();
                    showApp();
                } catch (Exception e) {
                    Toast.makeText(this, "Could not save entry",
                        Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

    void showSettings() {
        content.addView(text("Settings", 23, true));
        content.addView(text("Personalize your app.", 14, false));

        Button theme = makeButton(
            "Switch to " + (dark ? "Light" : "Dark") + " Theme");
        theme.setOnClickListener(v -> {
            dark = !dark;
            prefs.edit().putBoolean("dark", dark).apply();
            showApp();
        });
        addCard(theme);

        addCard(text("App: 2in1", 16, true));
        addCard(text("Storage: Saved on this device", 15, false));
        addCard(text(
            "Currency can be selected for every money entry.", 14, false));
    }
                                                    }
                 
