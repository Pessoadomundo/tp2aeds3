package oliv;

/**
 * Considerando que a ordem da árvore utilizada será 8
 */
public class Pagina {
    int[] ids;
    long[] posicoes;
    int[] filhos;
    byte qtdElementos;
    boolean eFolha;

    public Pagina(int ordem) {
        ids = new int[ordem - 1];
        posicoes = new long[ordem - 1];
        filhos = new int[ordem];
        qtdElementos = 0;
        eFolha = true;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[117];
        int pos = 0;
        bytes[pos++] = qtdElementos;
        bytes[pos++] = eFolha ? (byte) 1 : (byte) 0;
        // aqui, serao inseridos os bytes referentes aos filhos da pagina
        // antigamente o inicio desse for tinha como teto 8, mas como o array filhos nao tem sempre tamanho 8, o for nao fazia sentido

        for (int i = 0; i < filhos.length; i++) {
            byte[] bArr = TreeUtil.getByteArray(filhos[i]);
            for (int j = 0; j < 4; j++) {
                bytes[pos++] = bArr[j];
            }
            if (i > ids.length) {
                break;
            }
            // aqui, serao inseridos os bytes referentes aos ids da pagina
            bArr = TreeUtil.getByteArray(ids[i]);
            for (int j = 0; j < 4; j++) {
                bytes[pos++] = bArr[j];
            }
            // aqui, serao inseridos os bytes referentes as posicoes da pagina
            bArr = TreeUtil.getByteArray(posicoes[i]);
            for (int j = 0; j < 8; j++) {
                bytes[pos++] = bArr[j];
            }
        }
        return bytes;
    }

    public byte[] fromByteArray(byte[] bytes) {
        int pos = 0;
        qtdElementos = bytes[pos++];
        eFolha = bytes[pos++] == 1;
        for (int i = 0; i < filhos.length; i++) {
            byte[] bArr = new byte[4];
            for (int j = 0; j < 4; j++) {
                bArr[j] = bytes[pos++];
            }
            filhos[i] = TreeUtil.getIntFromByteArray(bArr);
            if (i > ids.length) {
                break;
            }
            bArr = new byte[4];
            for (int j = 0; j < 4; j++) {
                bArr[j] = bytes[pos++];
            }
            ids[i] = TreeUtil.getIntFromByteArray(bArr);
            bArr = new byte[8];
            for (int j = 0; j < 8; j++) {
                bArr[j] = bytes[pos++];
            }
            posicoes[i] = TreeUtil.getLongFromByteArray(bArr);
        }
        return bytes;
    }

}
