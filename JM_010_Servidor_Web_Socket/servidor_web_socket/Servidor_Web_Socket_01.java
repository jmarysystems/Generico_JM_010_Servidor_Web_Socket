package servidor_web_socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JTextArea;

/**
 *
 * @author NewUser
 */
public class Servidor_Web_Socket_01 {
    
    JTextArea jTextArea_01;
    
    public Servidor_Web_Socket_01(int int_porta, JTextArea jTextArea_012) throws IOException {
        
        jTextArea_01 = jTextArea_012;
        jTextArea_01.append( "Servidor Iniciado Com Sucesso!" + "\n" );
        iniciar(int_porta);
    }
    
    private void iniciar(int int_porta) throws IOException {
        /* cria um socket "servidor" associado a porta 8000
            já aguardando conexões
        */
        ServerSocket servidor = new ServerSocket(int_porta);
        //executor que limita a criação de threads a 20
        ExecutorService pool = Executors.newFixedThreadPool(20);
        
        while (true) {
            //cria uma nova thread para cada nova solicitacao de conexao
            jTextArea_01.append( "\n New - pool.execute( new Servidor_Web_Socket_02_ThreadConexao( servidor.accept(), jTextArea_01 ) );" );
            pool.execute( new Servidor_Web_Socket_02_ThreadConexao( servidor.accept(), jTextArea_01 ) );
        }
    }
    
}
