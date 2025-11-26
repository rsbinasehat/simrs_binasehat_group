/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bridging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author macbook
 */
public class ApiAdamLabs {

    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps, ps2;
    private ResultSet rs, rs2;
    private String URL = "", KEY = "", KODERS = "", requestJson = "", requestJson2 = "", stringbalik = "";
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private JsonNode root;
    private sekuel Sequel = new sekuel();
    private JsonNode response;
    private ObjectMapper mapper = new ObjectMapper();
    private int i = 0;

    public ApiAdamLabs() {
        super();
        try {
            URL = koneksiDB.URL_AdamLabs();
            KEY = koneksiDB.APIKEY_AdamLabs();
            KODERS = koneksiDB.KODERS_AdamLabs();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    public void kirimRalan(String nopermintaan) {
        try {
            ps = koneksi.prepareStatement(
                    "select permintaan_lab.noorder,permintaan_lab.no_rawat,reg_periksa.no_rkm_medis,"
                    + "pasien.no_ktp,pasien.nm_pasien,permintaan_lab.tgl_permintaan,"
                    + "if(permintaan_lab.jam_permintaan='00:00:00','',permintaan_lab.jam_permintaan) as jam_permintaan,"
                    + "permintaan_lab.diagnosa_klinis,permintaan_lab.informasi_tambahan,pasien.tgl_lahir,"
                    + "pasien.jk,pasien.alamat,propinsi.nm_prop,kabupaten.nm_kab,kecamatan.nm_kec,"
                    + "if(permintaan_lab.tgl_sampel='0000-00-00','',permintaan_lab.tgl_sampel) as tgl_sampel,"
                    + "if(permintaan_lab.jam_sampel='00:00:00','',permintaan_lab.jam_sampel) as jam_sampel,"
                    + "if(permintaan_lab.tgl_hasil='0000-00-00','',permintaan_lab.tgl_hasil) as tgl_hasil,"
                    + "if(permintaan_lab.jam_hasil='00:00:00','',permintaan_lab.jam_hasil) as jam_hasil,"
                    + "permintaan_lab.dokter_perujuk,dokter.nm_dokter,poliklinik.kd_poli,poliklinik.nm_poli,pasien.no_tlp,penjab.kd_pj,penjab.png_jawab "
                    + "from permintaan_lab inner join reg_periksa inner join pasien inner join dokter "
                    + "inner join poliklinik inner join penjab inner join propinsi inner join kabupaten "
                    + "inner join kecamatan on permintaan_lab.no_rawat=reg_periksa.no_rawat "
                    + "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj "
                    + "and permintaan_lab.dokter_perujuk=dokter.kd_dokter and reg_periksa.kd_poli=poliklinik.kd_poli "
                    + "and pasien.kd_prop=propinsi.kd_prop and pasien.kd_kab=kabupaten.kd_kab "
                    + "and pasien.kd_kec=kecamatan.kd_kec where permintaan_lab.noorder=?");
            try {
                ps.setString(1, nopermintaan);
                rs = ps.executeQuery();
                while (rs.next()) {
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type", "application/json;charset=UTF-8");
                    headers.add("x-api-key", KEY);
                    ps2 = koneksi.prepareStatement(
                            "select permintaan_detail_permintaan_lab.id_template,template_laboratorium.Pemeriksaan,"
                            + "template_laboratorium.urut from permintaan_detail_permintaan_lab "
                            + "inner join template_laboratorium on permintaan_detail_permintaan_lab.id_template=template_laboratorium.id_template "
                            + "where permintaan_detail_permintaan_lab.noorder=? order by template_laboratorium.kd_jenis_prw,template_laboratorium.urut desc");
                    try {
                        ps2.setString(1, rs.getString("noorder"));
                        rs2 = ps2.executeQuery();
                        requestJson2 = "";
                        while (rs2.next()) {
                            requestJson2
                                    = "{"
                                    + "\"kode_tindakan\": \"" + rs2.getString("id_template") + "\","
                                    + "\"nama_tindakan\": \"" + rs2.getString("Pemeriksaan") + "\""
                                    + "}," + requestJson2;
                        }
                        if (requestJson2.endsWith(",")) {
                            requestJson2 = requestJson2.substring(0, requestJson2.length() - 1);
                        }
                    } catch (Exception e) {
                        System.out.println("Notif 3 : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }

                    String dataNIK = padLeftWithZeros(rs.getString("no_ktp"), 16);

                    requestJson = "{"
                            + "\"registrasi\": {"
                            + "\"no_registrasi\": \"" + rs.getString("noorder") + "\","
                            + "\"diagnosa_awal\": \"" + rs.getString("diagnosa_klinis") + "\","
                            + "\"keterangan_klinis\": \"" + rs.getString("informasi_tambahan") + "\","
                            + "\"kode_rs\": \"" + KODERS + "\""
                            + "},"
                            + "\"pasien\": {"
                            + "\"nama\": \"" + rs.getString("nm_pasien") + "\","
                            + "\"no_rm\": \"" + rs.getString("no_rkm_medis") + "\","
                            + "\"jenis_kelamin\": \"" + rs.getString("jk") + "\","
                            + "\"alamat\": \"" + rs.getString("alamat") + "\","
                            + "\"no_telphone\": \"" + rs.getString("no_tlp") + "\","
                            + "\"tanggal_lahir\": \"" + rs.getString("tgl_lahir") + "\","
                            + "\"nik\": \"" + dataNIK + "\","
                            + "\"ras\": \"-\","
                            + "\"berat_badan\": \"-\","
                            + "\"jenis_registrasi\": \"Reguler / Cito\","
                            + "\"m_provinsi_id\": \"" + rs.getString("nm_prop") + "\","
                            + "\"m_kabupaten_id\": \"" + rs.getString("nm_kab") + "\","
                            + "\"m_kecamatan_id\": \"" + rs.getString("nm_kec") + "\""
                            + "},"
                            + "\"kode_dokter_pengirim\": \"" + rs.getString("dokter_perujuk") + "\","
                            + "\"nama_dokter_pengirim\": \"" + rs.getString("nm_dokter") + "\","
                            + "\"kode_unit_asal\": \"" + rs.getString("kd_poli") + "\","
                            + "\"nama_unit_asal\": \"" + rs.getString("nm_poli") + "\","
                            + "\"kode_penjamin\": \"" + rs.getString("kd_pj") + "\","
                            + "\"nama_penjamin\": \"" + rs.getString("png_jawab") + "\","
                            + "\"kode_icdt\": \"-\","
                            + "\"nama_icdt\": \"-\","
                            + "\"tindakan\": ["
                            + requestJson2
                            + "]"
                            + "}";
                    System.out.println("JSON : " + requestJson);
                    requestEntity = new HttpEntity(requestJson, headers);
                    stringbalik = getRest().exchange(URL + "/registrasi", HttpMethod.POST, requestEntity, String.class).getBody();
                    System.out.println("Response : " + stringbalik);
                    ResponseModel varResponse = mapper.readValue(stringbalik, ResponseModel.class);

                    if (varResponse.getStatus() == 200) {
                        JOptionPane.showMessageDialog(null, "Data berhasil dikirim ke server Adam Labs");
                    } else {
                        JOptionPane.showMessageDialog(null, "Data gagal dikirim ke server Adam Labs. Silahkan cek error di console.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
                if (e.toString().contains("UnknownHostException") || e.toString().contains("404")) {
                    JOptionPane.showMessageDialog(null, "Koneksi ke server Adam Labs terputus...!");
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
            if (ex.toString().contains("UnknownHostException") || ex.toString().contains("404")) {
                JOptionPane.showMessageDialog(null, "Koneksi ke server Adam Labs terputus...!");
            }
        }
    }

    public void kirimRanap(String nopermintaan) {
        try {
            ps = koneksi.prepareStatement(
                    "select permintaan_lab.noorder,permintaan_lab.no_rawat,reg_periksa.no_rkm_medis,"
                    + "pasien.no_ktp,pasien.nm_pasien,permintaan_lab.tgl_permintaan,"
                    + "if(permintaan_lab.jam_permintaan='00:00:00','',permintaan_lab.jam_permintaan) as jam_permintaan,"
                    + "permintaan_lab.diagnosa_klinis,permintaan_lab.informasi_tambahan,pasien.tgl_lahir,"
                    + "pasien.jk,pasien.alamat,propinsi.nm_prop,kabupaten.nm_kab,kecamatan.nm_kec,"
                    + "if(permintaan_lab.tgl_sampel='0000-00-00','',permintaan_lab.tgl_sampel) as tgl_sampel,"
                    + "if(permintaan_lab.jam_sampel='00:00:00','',permintaan_lab.jam_sampel) as jam_sampel,"
                    + "if(permintaan_lab.tgl_hasil='0000-00-00','',permintaan_lab.tgl_hasil) as tgl_hasil,"
                    + "if(permintaan_lab.jam_hasil='00:00:00','',permintaan_lab.jam_hasil) as jam_hasil,"
                    + "permintaan_lab.dokter_perujuk,dokter.nm_dokter,kamar_inap.kd_kamar,bangsal.nm_bangsal,"
                    + "pasien.no_tlp,penjab.kd_pj,penjab.png_jawab from permintaan_lab inner join reg_periksa "
                    + "inner join pasien inner join dokter inner join bangsal inner join kamar "
                    + "inner join kamar_inap inner join penjab inner join propinsi inner join kabupaten "
                    + "inner join kecamatan on permintaan_lab.no_rawat=reg_periksa.no_rawat "
                    + "and reg_periksa.no_rkm_medis=pasien.no_rkm_medis and reg_periksa.kd_pj=penjab.kd_pj "
                    + "and permintaan_lab.dokter_perujuk=dokter.kd_dokter "
                    + "and kamar.kd_bangsal=bangsal.kd_bangsal and reg_periksa.no_rawat=kamar_inap.no_rawat "
                    + "and kamar_inap.kd_kamar=kamar.kd_kamar and pasien.kd_prop=propinsi.kd_prop "
                    + "and pasien.kd_kab=kabupaten.kd_kab and pasien.kd_kec=kecamatan.kd_kec "
                    + "where permintaan_lab.noorder=?");
            try {
                ps.setString(1, nopermintaan);
                rs = ps.executeQuery();
                while (rs.next()) {
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Content-Type", "application/json;charset=UTF-8");
                    headers.add("x-api-key", KEY);
                    ps2 = koneksi.prepareStatement(
                            "select permintaan_detail_permintaan_lab.id_template,template_laboratorium.Pemeriksaan,"
                            + "template_laboratorium.urut from permintaan_detail_permintaan_lab "
                            + "inner join template_laboratorium on permintaan_detail_permintaan_lab.id_template=template_laboratorium.id_template "
                            + "where permintaan_detail_permintaan_lab.noorder=? order by template_laboratorium.kd_jenis_prw,template_laboratorium.urut desc");
                    try {
                        ps2.setString(1, rs.getString("noorder"));
                        rs2 = ps2.executeQuery();
                        requestJson2 = "";
                        while (rs2.next()) {
                            requestJson2
                                    = "{"
                                    + "\"kode_tindakan\": \"" + rs2.getString("id_template") + "\","
                                    + "\"nama_tindakan\": \"" + rs2.getString("Pemeriksaan") + "\""
                                    + "}," + requestJson2;
                        }
                        if (requestJson2.endsWith(",")) {
                            requestJson2 = requestJson2.substring(0, requestJson2.length() - 1);
                        }
                    } catch (Exception e) {
                        System.out.println("Notif 3 : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }

                    String dataNIK = padLeftWithZeros(rs.getString("no_ktp"), 16);

                    requestJson = "{"
                            + "\"registrasi\": {"
                            + "\"no_registrasi\": \"" + rs.getString("noorder") + "\","
                            + "\"diagnosa_awal\": \"" + rs.getString("diagnosa_klinis") + "\","
                            + "\"keterangan_klinis\": \"" + rs.getString("informasi_tambahan") + "\","
                            + "\"kode_rs\": \"" + KODERS + "\""
                            + "},"
                            + "\"pasien\": {"
                            + "\"nama\": \"" + rs.getString("nm_pasien") + "\","
                            + "\"no_rm\": \"" + rs.getString("no_rkm_medis") + "\","
                            + "\"jenis_kelamin\": \"" + rs.getString("jk") + "\","
                            + "\"alamat\": \"" + rs.getString("alamat") + "\","
                            + "\"no_telphone\": \"" + rs.getString("no_tlp") + "\","
                            + "\"tanggal_lahir\": \"" + rs.getString("tgl_lahir") + "\","
                            + "\"nik\": \"" + dataNIK + "\","
                            + "\"ras\": \"-\","
                            + "\"berat_badan\": \"-\","
                            + "\"jenis_registrasi\": \"Reguler / Cito\","
                            + "\"m_provinsi_id\": \"" + rs.getString("nm_prop") + "\","
                            + "\"m_kabupaten_id\": \"" + rs.getString("nm_kab") + "\","
                            + "\"m_kecamatan_id\": \"" + rs.getString("nm_kec") + "\""
                            + "},"
                            + "\"kode_dokter_pengirim\": \"" + rs.getString("dokter_perujuk") + "\","
                            + "\"nama_dokter_pengirim\": \"" + rs.getString("nm_dokter") + "\","
                            + "\"kode_unit_asal\": \"" + rs.getString("kd_kamar") + "\","
                            + "\"nama_unit_asal\": \"" + rs.getString("nm_bangsal") + "\","
                            + "\"kode_penjamin\": \"" + rs.getString("kd_pj") + "\","
                            + "\"nama_penjamin\": \"" + rs.getString("png_jawab") + "\","
                            + "\"kode_icdt\": \"-\","
                            + "\"nama_icdt\": \"-\","
                            + "\"tindakan\": ["
                            + requestJson2
                            + "]"
                            + "}";
                    System.out.println("JSON : " + requestJson);
                    requestEntity = new HttpEntity(requestJson, headers);
                    stringbalik = getRest().exchange(URL + "/registrasi", HttpMethod.POST, requestEntity, String.class).getBody();
                    System.out.println("Response : " + stringbalik);
                    ResponseModel varResponse = mapper.readValue(stringbalik, ResponseModel.class);

                    if (varResponse.getStatus() == 200) {
                        JOptionPane.showMessageDialog(null, "Data berhasil dikirim ke server Adam Labs");
                    }else if(varResponse.getStatus()==0){
                        JOptionPane.showMessageDialog(null, varResponse.getMessageString());
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Data gagal dikirim ke server Adam Labs. Silahkan cek error di console.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
                if (e.toString().contains("UnknownHostException") || e.toString().contains("404")) {
                    JOptionPane.showMessageDialog(null, "Koneksi ke server Adam Labs terputus...!");
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
            if (ex.toString().contains("UnknownHostException") || ex.toString().contains("404")) {
                JOptionPane.showMessageDialog(null, "Koneksi ke server Adam Labs terputus...!");
            }
        }
    }

    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        javax.net.ssl.TrustManager[] trustManagers = {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            }
        };
        sslContext.init(null, trustManagers, new SecureRandom());
        SSLSocketFactory sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme scheme = new Scheme("https", 443, sslFactory);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }

    public static String padLeftWithZeros(String input, int length) {
        return String.format("%1$" + length + "s", input).replace(' ', '0');
    }

    public ResponseModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        ResponseModel response = new ResponseModel();
        response.setSuccess(node.get("success").asBoolean());

        if (node.has("status")) {
            response.setStatus(node.get("status").asInt());
        }

        // message bisa string atau array
        JsonNode messageNode = node.get("message");
        if (messageNode != null) {
            if (messageNode.isTextual()) {
                response.setMessageString(messageNode.asText());
            } else if (messageNode.isArray()) {
                List<MessageItem> items = new ArrayList<>();
                for (JsonNode itemNode : messageNode) {
                    items.add(mapper.treeToValue(itemNode, MessageItem.class));
                }
                response.setMessage(items);
            }
        }

        return response;
    }

}
