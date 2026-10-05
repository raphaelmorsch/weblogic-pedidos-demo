package br.com.exemplo.legado;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.persistence.*;
import javax.annotation.Resource;
import javax.jms.*;
import weblogic.logging.NonCatalogLogger;
@Stateless @LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PedidoService {
 @PersistenceContext(unitName="PedidosPU") private EntityManager em;
 @Resource(lookup="jms/PedidosConnectionFactory") private ConnectionFactory factory;
 @Resource(lookup="jms/PedidosQueue") private Queue queue;
 private static final NonCatalogLogger LOG = new NonCatalogLogger("Pedidos");
 public Long criar(String cliente, BigDecimal valor) {
  validar(cliente, valor);
  Pedido pedido = new Pedido(cliente.trim(), valor);
  em.persist(pedido); em.flush();
  // Banco + JMS dependem da configuração XA/JTA do domínio para atomicidade.
  try (JMSContext context = factory.createContext()) {
   context.createProducer().send(queue, "PEDIDO:" + pedido.getId());
  }
  LOG.info("Pedido criado: " + pedido.getId());
  return pedido.getId();
 }
 static void validar(String cliente, BigDecimal valor) {
  if (cliente == null || cliente.trim().isEmpty() || cliente.trim().length() > 120)
   throw new IllegalArgumentException("Cliente obrigatório, até 120 caracteres");
  if (valor == null || valor.signum() <= 0 || valor.scale() > 2 || valor.compareTo(new BigDecimal("9999999999.99")) > 0)
   throw new IllegalArgumentException("Valor positivo, até 9999999999.99 e duas casas decimais");
 }
}
