package br.com.exemplo.legado;
import javax.ejb.*;
import javax.jms.*;
@MessageDriven(name="PedidoListener", activationConfig={
 @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
 @ActivationConfigProperty(propertyName="destinationLookup", propertyValue="jms/PedidosQueue")})
public class PedidoListener implements MessageListener {
 @Override public void onMessage(Message message) {
  try {
   if (message instanceof TextMessage) {
    // Efeito em arquivo não participa da transação JMS: reentrega pode duplicar linhas.
    LegacyAudit.append(((TextMessage)message).getText());
   }
  } catch (Exception e) { throw new EJBException("Falha na auditoria", e); }
 }
}
