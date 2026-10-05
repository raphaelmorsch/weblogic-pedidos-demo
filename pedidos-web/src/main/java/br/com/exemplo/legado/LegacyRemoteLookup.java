package br.com.exemplo.legado;
import java.util.Hashtable;
import javax.naming.*;
public class LegacyRemoteLookup {
 // Exemplo analisável; não é chamado pelo fluxo HTTP nem pelo build.
 public Object buscar(String nome) throws NamingException {
  Hashtable<String,String> env = new Hashtable<String,String>();
  env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
  env.put(Context.PROVIDER_URL, "t3://legacy-weblogic.invalid:7001");
  Context ctx = new InitialContext(env);
  try { return ctx.lookup(nome); } finally { ctx.close(); }
 }
}
