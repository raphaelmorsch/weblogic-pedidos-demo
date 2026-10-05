package br.com.exemplo.legado;
import java.io.IOException;
import java.math.BigDecimal;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.*;
public class PedidoServlet extends HttpServlet {
 @EJB private PedidoService service;
 @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
  resp.setContentType("text/plain;charset=UTF-8");
  resp.getWriter().println("Pedidos legado: POST cliente e valor para criar um pedido.");
 }
 @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
  req.setCharacterEncoding("UTF-8");
  try {
   String raw = req.getParameter("valor");
   if (raw == null) throw new IllegalArgumentException("Valor obrigatório");
   Long id = service.criar(req.getParameter("cliente"), new BigDecimal(raw));
   // Estado de sessão acoplado ao nó/replicação do servidor.
   req.getSession().setAttribute("ultimoPedido", id);
   resp.setStatus(201); resp.setContentType("text/plain;charset=UTF-8");
   resp.getWriter().println(id);
  } catch (IllegalArgumentException e) { resp.sendError(400, e.getMessage()); }
 }
}
