package oliv;

public class TreeUtil {
    /**
     * Recebe (parâmetro) um array de bytes e retorna um float.
     * 
     * @param bArr
     * @return Retorna um float que representa o array de bytes bArr.
     */
    public static byte[] getByteArray(long l) {
        byte[] res = new byte[8];
        for (int i = 7; i >= 0; i--) {
            res[i] = (byte) l;
            l = l >> 8;
        }

        return res;
    }

    /**
     * Recebe (parâmetro) um array de bytes e retorna um float.
     * 
     * @param bArr
     * @return Retorna um float que representa o array de bytes bArr.
     */
    public static byte[] getByteArray(int n) {
        byte[] res = new byte[4];
        for (int i = 3; i >= 0; i--) {
            res[i] = (byte) n;
            n = n >> 8;
        }

        return res;
    }

    /**
     * Recebe (parâmetro) um array de bytes e retorna um float.
     * 
     * @param bArr
     * @return Retorna um float que representa o array de bytes bArr.
     */
    public static float getFloatFromByteArray(byte[] bArr) {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            n = n << 8;
            n = n | (bArr[i] & 0xFF);
        }
        return Float.intBitsToFloat(n);
    }

    /**
     * Recebe (parâmetro) um array de bytes e retorna um int.
     * 
     * @param bArr
     * @return Retorna um int que representa o array de bytes bArr.
     */
    public static int getIntFromByteArray(byte[] bArr) {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            n = n << 8;
            n = n | (bArr[i] & 0xFF);
        }
        return n;
    }

    /**
     * Recebe (parâmetro) um array de bytes e retorna um long.
     * 
     * @param bArr
     * @return Retorna um long que representa o array de bytes bArr.
     */
    public static long getLongFromByteArray(byte[] bArr) {
        long l = 0;
        for (int i = 0; i < 8; i++) {
            l = l << 8;
            l = l | (bArr[i] & 0xFF);
        }
        return l;
    }
}
