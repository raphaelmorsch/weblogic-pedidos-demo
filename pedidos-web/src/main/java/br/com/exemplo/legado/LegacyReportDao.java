package br.com.exemplo.legado;
import java.sql.*;
import javax.naming.*;
import javax.sql.DataSource;
public class LegacyReportDao {
 public int contarPedidos() throws NamingException, SQLException {
  InitialContext naming = new InitialContext();
  try {
   DataSource ds = (DataSource) naming.lookup("java:comp/env/jdbc/PedidosDS");
   try (Connection c=ds.getConnection(); PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM PEDIDO"); ResultSet rs=s.executeQuery()) {
    rs.next(); return rs.getInt(1);
   }
  } finally { naming.close(); }
 }
}
