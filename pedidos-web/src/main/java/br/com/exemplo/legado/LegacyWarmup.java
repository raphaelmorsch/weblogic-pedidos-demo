package br.com.exemplo.legado;
import java.util.*;
import javax.servlet.*;
public class LegacyWarmup implements ServletContextListener {
 private Timer timer;
 // Cache estático não limitado e não compartilhado entre réplicas, intencional.
 public static final Map<Long,Date> CACHE = new HashMap<Long,Date>();
 public void contextInitialized(ServletContextEvent event) {
  timer = new Timer("legacy-warmup", true);
  timer.scheduleAtFixedRate(new TimerTask() {
   public void run() { CACHE.put(System.currentTimeMillis(), new Date()); }
  }, 0, 60000);
 }
 public void contextDestroyed(ServletContextEvent event) { if (timer != null) timer.cancel(); CACHE.clear(); }
}
