import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class ParIdEndereco {
    int id;
    long enderecoArquivo;

    public ParIdEndereco(int id, long endereco) {
        this.id = id;
        this.enderecoArquivo = endereco;
    }
}

class Pagina {
    static final int TAMANHO_PAGINA = 2048; // Tamanho máximo de uma página em bytes
    static final int TAMANHO_REGISTRO = 12; // Tamanho de um par de valores (id e endereço) em bytes
    static final int ORDEM = 8; // Ordem da árvore B

    ArrayList<ParIdEndereco> registros;

    public Pagina() {
        registros = new ArrayList<>();
    }

    public byte[] toByteArray() {
        byte[] byteArray = new byte[TAMANHO_PAGINA];
        int offset = 0;

        for (ParIdEndereco registro : registros) {
            byte[] idBytes = intToBytes(registro.id);
            byte[] enderecoBytes = longToBytes(registro.enderecoArquivo);

            System.arraycopy(idBytes, 0, byteArray, offset, TAMANHO_REGISTRO / 2);
            System.arraycopy(enderecoBytes, 0, byteArray, offset + TAMANHO_REGISTRO / 2, TAMANHO_REGISTRO / 2);

            offset += TAMANHO_REGISTRO;
        }

        return byteArray;
    }

    public static Pagina fromByteArray(byte[] byteArray) {
        Pagina pagina = new Pagina();
        int offset = 0;

        while (offset < byteArray.length) {
            byte[] idBytes = new byte[TAMANHO_REGISTRO / 2];
            byte[] enderecoBytes = new byte[TAMANHO_REGISTRO / 2];

            System.arraycopy(byteArray, offset, idBytes, 0, TAMANHO_REGISTRO / 2);
            System.arraycopy(byteArray, offset + TAMANHO_REGISTRO / 2, enderecoBytes, 0, TAMANHO_REGISTRO / 2);

            int id = bytesToInt(idBytes);
            long endereco = bytesToLong(enderecoBytes);

            pagina.registros.add(new ParIdEndereco(id, endereco));
            offset += TAMANHO_REGISTRO;
        }

        return pagina;
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    private static byte[] longToBytes(long value) {
        return new byte[]{
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    private static long bytesToLong(byte[] bytes) {
        return ((long) (bytes[0] & 0xFF) << 56) |
                ((long) (bytes[1] & 0xFF) << 48) |
                ((long) (bytes[2] & 0xFF) << 40) |
                ((long) (bytes[3] & 0xFF) << 32) |
                ((long) (bytes[4] & 0xFF) << 24) |
                ((long) (bytes[5] & 0xFF) << 16) |
                ((long) (bytes[6] & 0xFF) << 8) |
                ((long) (bytes[7] & 0xFF));
    }
}

public class ArvoreB {
    private static final String NOME_ARQUIVO = "arvore_b.bin";

    private RandomAccessFile arquivo;

    public ArvoreB() throws IOException {
        arquivo = new RandomAccessFile(NOME_ARQUIVO, "rw");
        if (arquivo.length() == 0) {
            // Se o arquivo estiver vazio, cria uma página vazia como nó raiz
            Pagina paginaVazia = new Pagina();
            try {
                arquivo.write(paginaVazia.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void inserir(int id, long endereco) throws IOException {
        long enderecoAtual = 0; // Começa na raiz

        while (true) {
            Pagina paginaAtual = lerPagina(enderecoAtual);

            if (paginaAtual.registros.size() < Pagina.ORDEM - 1) { // Ordem da árvore é fixa em 8
                // Inserção na página atual
                int indice = 0;
                while (indice < paginaAtual.registros.size() && id > paginaAtual.registros.get(indice).id) {
                    indice++;
                }
                paginaAtual.registros.add(indice, new ParIdEndereco(id, endereco));
                escreverPagina(enderecoAtual, paginaAtual);
                break;
            } else {
                // Dividir a página atual
                Pagina novaPagina = new Pagina();
                novaPagina.registros.addAll(paginaAtual.registros.subList(4, 7)); // Ordem da árvore é fixa em 8, então 4 é o meio
                paginaAtual.registros.subList(4, 7).clear(); // Ordem da árvore é fixa em 8, então 4 é o meio

                escreverPagina(enderecoAtual, paginaAtual);

                long enderecoNovaPagina = arquivo.length(); // Adiciona a nova página no final do arquivo
                escreverPagina(enderecoNovaPagina, novaPagina);

                // Atualiza o próximo nó da página atual para apontar para a nova página
                paginaAtual.registros.get(3).enderecoArquivo = enderecoNovaPagina;
                escreverPagina(enderecoAtual, paginaAtual);

                // Verifica se precisa subir um nível na árvore
                if (id < paginaAtual.registros.get(4).id) { // Ordem da árvore é fixa em 8, então 4 é o meio
                    enderecoAtual = enderecoNovaPagina;
                } else {
                    enderecoAtual = paginaAtual.registros.get(3).enderecoArquivo;
                }
            }
        }
    }

    public boolean buscar(int id) throws IOException {
        long enderecoAtual = 0; // Começa na raiz

        while (enderecoAtual != -1) {
            Pagina paginaAtual = lerPagina(enderecoAtual);

            int indice = 0;
            while (indice < paginaAtual.registros.size() && id > paginaAtual.registros.get(indice).id) {
                indice++;
            }
            if (indice < paginaAtual.registros.size() && id == paginaAtual.registros.get(indice).id) {
                return true;
            }
            enderecoAtual = indice < paginaAtual.registros.size() ? paginaAtual.registros.get(indice).enderecoArquivo : -1;
        }
        return false;
    }

    private void escreverPagina(long endereco, Pagina pagina) throws IOException {
        arquivo.seek(endereco);
        arquivo.write(pagina.toByteArray());
    }

    private Pagina lerPagina(long endereco) throws IOException {
        arquivo.seek(endereco);
        byte[] byteArray = new byte[Pagina.TAMANHO_PAGINA];
        arquivo.read(byteArray);
        return Pagina.fromByteArray(byteArray);
    }

    public static void main(String[] args) {
        try {
            ArvoreB arvoreB = new ArvoreB();
            arvoreB.inserir(1, 1000);
            arvoreB.inserir(3, 2000);
            arvoreB.inserir(7, 3000);
            arvoreB.inserir(10, 4000);
            arvoreB.inserir(11, 5000);
            arvoreB.inserir(13, 6000);
            arvoreB.inserir(14, 7000);
            arvoreB.inserir(15, 8000);
            arvoreB.inserir(18, 9000);
            arvoreB.inserir(16, 10000);
            arvoreB.inserir(19, 11000);
            arvoreB.inserir(24, 12000);
            arvoreB.inserir(25, 13000);
            arvoreB.inserir(26, 14000);
            arvoreB.inserir(21, 15000);
            arvoreB.inserir(4, 16000);
            arvoreB.inserir(5, 17000);
            arvoreB.inserir(20, 18000);
            arvoreB.inserir(22, 19000);
            arvoreB.inserir(2, 20000);
            System.out.println(arvoreB.buscar(20)); // Saída: true
            System.out.println(arvoreB.buscar(21)); // Saída: true
            System.out.println(arvoreB.buscar(5));  // Saída: true
            System.out.println(arvoreB.buscar(0));  // Saída: false
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}