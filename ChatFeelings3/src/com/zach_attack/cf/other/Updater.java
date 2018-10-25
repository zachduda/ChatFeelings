package com.zach_attack.cf.other;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.bukkit.plugin.java.JavaPlugin;

public class Updater
	{
	  private int project;
	  private URL checkURL;
	  private String newVersion;
	  private JavaPlugin plugin;
	  
	  
	  public Updater(JavaPlugin plugin, int id)
	  {
	    this.plugin = plugin;
	    this.newVersion = plugin.getDescription().getVersion();
	    this.project = id;
	    try
	    {
	      this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
	    }
	    catch (MalformedURLException e)
	    {
            this.plugin.getLogger().warning("Uh Oh! Could not establish connection with update server.");
            this.plugin.getLogger().warning("Please check your internet connection.");
	    }
	  }
	  
	  public JavaPlugin getPlugin()
	  {
	    return this.plugin;
	  }
	  
	  public String getLatestVersion()
	    throws Exception
	  {
	    URLConnection uc = this.checkURL.openConnection();
	    this.newVersion = new BufferedReader(new InputStreamReader(uc.getInputStream())).readLine();
	    return this.newVersion;
	  }
	  
	  public String getResourceURL()
	  {
	    return "https://www.spigotmc.org/resources/" + this.project;
	  }
	  
	  public boolean checkForUpdates()
	    throws Exception
	  {
	    URLConnection uc = this.checkURL.openConnection();
	    this.newVersion = new BufferedReader(new InputStreamReader(uc.getInputStream())).readLine();
	    return (!(this.getLatestVersion().equals("v" + plugin.getDescription().getVersion())));
	}

}
