package mindcet.natureg.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public  class Language {
    private final String [] languages = {"English","French","Russian","Hebrew","Arabic","Spanish"};
    private String defaultLanguage;
    private Context context;
    private Configuration configuration;

    public Language(Context con){
        context = con;
        configuration = new Configuration();
        defaultLanguage = loadUserLanguage();
        setLanguage(defaultLanguage);
    }

    public String loadUserLanguage(){
        SharedPreferences preferences = context.getSharedPreferences("Settings",MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "English");
        return language;
    }

    public void setLanguage(String chosenLang) {
        Locale locale = new Locale(chosenLang);
        Locale.setDefault(locale);
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,context.getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = context.getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",chosenLang);
        editor.apply();
    }

    public String[] getLanguages() {
        return languages;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
