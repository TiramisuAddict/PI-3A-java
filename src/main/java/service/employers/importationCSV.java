package service.employers;

import entities.employers.employe;
import entities.employers.role;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;

public class importationCSV {

    public static class ImportResult {
        private final int succes;
        private final int erreurs;
        public ImportResult(int succes, int erreurs) {
            this.succes = succes;
            this.erreurs = erreurs;
        }
        public int getSucces() { return succes; }
        public int getErreurs() { return erreurs; }
        public String getMessage() {
            return succes + " importé" + (succes > 1 ? "s" : "") + " avec succès." + (erreurs > 0 ? "\n" + erreurs + " erreur(s)." : "");
        }
    }

    public static ImportResult importerEmployesCSV(File file, int idEntreprise, employeCRUD crud) {
        int ok = 0, err = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int i = 0;

            while ((line = br.readLine()) != null) {
                if (i++ == 0 && line.toLowerCase().contains("nom")) continue;
                String[] p = line.split(";", -1);
                if (p.length < 6) {
                    err++;
                    continue;
                }
                try {
                    employe e = new employe();
                    e.setNom(p[0].trim());
                    e.setPrenom(p[1].trim());
                    e.setE_mail(p[2].trim());
                    e.setTelephone(Integer.parseInt(p[3].trim()));
                    e.setPoste(p[4].trim());
                    e.setRole(parseRole(p[5].trim()));
                    e.setDate_embauche(p.length > 6 && !p[6].isEmpty() ? LocalDate.parse(p[6].trim()) : null);
                    e.setIdEntreprise(idEntreprise);

                    crud.add(e);
                    ok++;
                } catch (Exception ex) {
                    err++;
                }
            }
        } catch (Exception e) {
            err++;
        }

        return new ImportResult(ok, err);
    }

    private static role parseRole(String t) {
        if (t == null) return role.EMPLOYE;
        String v = t.trim().toLowerCase();
        if (v.contains("rh")) return role.RH;
        if (v.contains("chef")) return role.CHEF_PROJET;
        if (v.contains("admin")) return role.ADMINISTRATEUR_ENTREPRISE;
        return role.EMPLOYE;
    }
}