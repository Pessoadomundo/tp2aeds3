import java.io.FileNotFoundException;
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

class NoArvoreB {
    static final int TAMANHO_REGISTRO = 2048;
    static final int TAMANHO_PAR = 12;
    static final int TAMANHO_PONTEIRO = 8;

    ArrayList<ParIdEndereco> pares;
    long proximoNo;

    public NoArvoreB() {
        pares = new ArrayList<>();
        proximoNo = -1;
    }

    public byte[] toByteArray() {
        byte[] byteArray = new byte[TAMANHO_REGISTRO];
        int offset = 0;

        for (ParIdEndereco par : pares) {
            byte[] idBytes = intToBytes(par.id);
            byte[] enderecoBytes = longToBytes(par.enderecoArquivo);

            System.arraycopy(idBytes, 0, byteArray, offset, TAMANHO_PAR / 2);
            System.arraycopy(enderecoBytes, 0, byteArray, offset + TAMANHO_PAR / 2, TAMANHO_PAR / 2);

            offset += TAMANHO_PAR;
        }

        byte[] proximoNoBytes = longToBytes(proximoNo);
        System.arraycopy(proximoNoBytes, 0, byteArray, TAMANHO_REGISTRO - TAMANHO_PONTEIRO, TAMANHO_PONTEIRO);

        return byteArray;
    }

    public static NoArvoreB fromByteArray(byte[] byteArray) {
        NoArvoreB no = new NoArvoreB();
        int offset = 0;

        while (offset < byteArray.length - TAMANHO_PONTEIRO) {
            byte[] idBytes = new byte[TAMANHO_PAR / 2];
            byte[] enderecoBytes = new byte[TAMANHO_PAR / 2];

            System.arraycopy(byteArray, offset, idBytes, 0, TAMANHO_PAR / 2);
            System.arraycopy(byteArray, offset + TAMANHO_PAR / 2, enderecoBytes, 0, TAMANHO_PAR / 2);

            int id = bytesToInt(idBytes);
            long endereco = bytesToLong(enderecoBytes);

            no.pares.add(new ParIdEndereco(id, endereco));
            offset += TAMANHO_PAR;
        }

        byte[] proximoNoBytes = new byte[TAMANHO_PONTEIRO];
        System.arraycopy(byteArray, TAMANHO_REGISTRO - TAMANHO_PONTEIRO, proximoNoBytes, 0, TAMANHO_PONTEIRO);

        no.proximoNo = bytesToLong(proximoNoBytes);

        return no;
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value };
    }

    private static int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    private static byte[] longToBytes(long value) {
        return new byte[] {
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value };
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

    public ArvoreB() throws FileNotFoundException {
        arquivo = new RandomAccessFile(NOME_ARQUIVO, "rw");
    }

    public void inserir(int id, long endereco) throws IOException {
        NoArvoreB noAtual = lerNo(0);

        while (true) {
            if (noAtual.pares.size() < 7) {

                int indice = 0;
                while (indice < noAtual.pares.size() && id > noAtual.pares.get(indice).id) {
                    indice++;
                }
                noAtual.pares.add(indice, new ParIdEndereco(id, endereco));
                escreverNo(noAtual);
                break;
            } else {

                NoArvoreB novoNo = new NoArvoreB();
                novoNo.pares.addAll(noAtual.pares.subList(4, 7));
                noAtual.pares.subList(4, 7).clear();

                novoNo.proximoNo = noAtual.proximoNo;
                noAtual.proximoNo = arquivo.length();
                escreverNo(noAtual);
                escreverNo(novoNo);

                if (id < noAtual.pares.get(4).id) {
                    noAtual = novoNo;
                } else {
                    noAtual = lerNo(noAtual.proximoNo);
                }
            }
        }
    }

    public boolean buscar(int id) throws IOException {
        NoArvoreB noAtual = lerNo(0);

        while (noAtual != null) {
            int indice = 0;
            while (indice < noAtual.pares.size() && id > noAtual.pares.get(indice).id) {
                indice++;
            }
            if (indice < noAtual.pares.size() && id == noAtual.pares.get(indice).id) {
                return true;
            }
            noAtual = noAtual.proximoNo != -1 ? lerNo(noAtual.proximoNo) : null;
        }
        return false;
    }

    private void escreverNo(NoArvoreB no) throws IOException {
        arquivo.seek(arquivo.length());
        arquivo.write(no.toByteArray());
    }

    private NoArvoreB lerNo(long endereco) throws IOException {
        if (endereco == -1) {
            return null;
        }
        arquivo.seek(endereco);
        byte[] byteArray = new byte[NoArvoreB.TAMANHO_REGISTRO];
        arquivo.read(byteArray);
        return NoArvoreB.fromByteArray(byteArray);
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
            System.out.println(arvoreB.buscar(20));
            System.out.println(arvoreB.buscar(21));
            System.out.println(arvoreB.buscar(5));
            System.out.println(arvoreB.buscar(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
