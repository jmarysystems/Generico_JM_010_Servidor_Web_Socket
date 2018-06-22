package servidor_web_socket;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author NewUser
 */
public class Servidor_Web_Socket_02_ThreadConexao implements Runnable {
    
    private final Socket socket;
    private boolean conectado;
    JTextArea jTextArea_01;

    public Servidor_Web_Socket_02_ThreadConexao( Socket socket, JTextArea jTextArea_012 ) {
        jTextArea_01 = jTextArea_012;
        this.socket = socket;
    }

    @Override
    public void run() {
        
        conectado = true;
        //imprime na tela o IP do cliente 
        System.out.println( socket.getInetAddress() );        
        jTextArea_01.append( "\n" + "Cliente Conectado - IP: " + socket.getInetAddress().toString() + "\n" );
        
        while(conectado){
            try {
                //cria uma requisicao a partir do InputStream do cliente
                Servidor_Web_Socket_03_RequisicaoHTTP requisicao = Servidor_Web_Socket_03_RequisicaoHTTP.lerRequisicao(socket.getInputStream(),jTextArea_01);
                //se a conexao esta marcada para se mantar viva entao seta keepalive e o timeout
                if (requisicao.isManterViva()) {
                    socket.setKeepAlive(true);
                    socket.setSoTimeout((int) requisicao.getTempoLimite());
                } else {
                    //se nao seta um valor menor suficiente para uma requisicao
                    socket.setSoTimeout(300);
                }
                                
                //se o caminho foi igual a / entao deve pegar o /index.html
                String url_dir = System.getProperty("user.dir") + "/00_Externo/";
                if (requisicao.getRecurso().equals("/")) {
                    requisicao.setRecurso( url_dir + "blog/index/index.html" );
                }
                //abre o arquivo pelo caminho  
                File arquivo = new File( requisicao.getRecurso() );

                Servidor_Web_Socket_04_RespostaHTTP resposta;

                //se o arquivo existir então criamos a reposta de sucesso, com status 200
                if (arquivo.exists()) {
                    resposta = new Servidor_Web_Socket_04_RespostaHTTP(requisicao.getProtocolo(), 200, "OK");
                } else {
                    //se o arquivo não existe então criamos a reposta de erro, com status 404
                    resposta = new Servidor_Web_Socket_04_RespostaHTTP(requisicao.getProtocolo(), 404, "Not Found");
                    arquivo = new File("404.html");
                }
                //lê todo o conteúdo do arquivo para bytes e gera o conteudo de resposta
                resposta.setConteudoResposta(Files.readAllBytes(arquivo.toPath()));
                //converte o formato para o GMT espeficicado pelo protocolo HTTP
                //cria um formato para o GMT espeficicado pelo HTTP
                SimpleDateFormat formatador = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss", Locale.ENGLISH);
                formatador.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date data = new Date();
                //Formata a dara para o padrao
                String dataFormatada = formatador.format(data) + " GMT";
                //cabeçalho padrão da resposta HTTP/1.1
                resposta.setCabecalho("Location", "http://localhost:8000/");
                resposta.setCabecalho("Date", dataFormatada);
                resposta.setCabecalho("Server", "MeuServidor/1.0");
                resposta.setCabecalho("Content-Type", "text/html");
                resposta.setCabecalho("Content-Length", resposta.getTamanhoResposta());
                //cria o canal de resposta utilizando o outputStream
                resposta.setSaida(socket.getOutputStream());
                resposta.enviar();
            } catch (IOException ex) {
                //quando o tempo limite terminar encerra a thread
                if (ex instanceof SocketTimeoutException) {
                    try {
                        conectado = false;
                        socket.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(Servidor_Web_Socket_02_ThreadConexao.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }

        }
    }

}
