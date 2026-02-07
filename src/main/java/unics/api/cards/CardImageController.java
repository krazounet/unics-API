package unics.api.cards;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import aiGenerated.CardRender;
import aiGenerated.RenderProfile;
import dbPG18.DbUtil;
import dbPG18.JdbcCardRenderDao;
import dbPG18.JdbcCardSnapshotDao;
import unics.godot.RenderBackCard;
import unics.snapshot.CardSnapshot;

@RestController
public class CardImageController {

    // ─────────────────────────────────────────────
    // CONFIG
    // ─────────────────────────────────────────────

    // Racine filesystem des données (images générées)
    // La DB stocke : images/DEFAULT/xxxx.png
    private static final Path IMAGE_ROOT =
        Paths.get("C:/Users/fabie/eclipse-workspace/unics");

    // Dossier cache disque (persistant)
    private static final Path CACHE_DIR =
        Paths.get("C:/unics-cache/cards");

    // Profil de rendu
    private static final RenderProfile PROFILE =
        RenderProfile.DEFAULT;

    
    // ─────────────────────────────────────────────
    // ENDPOINT
    // ─────────────────────────────────────────────

    @GetMapping(
        value = "/api/cards/{snapshotId}/image",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getCardImage(
        @PathVariable UUID snapshotId
    ) {
        try {
            // ─────────────────────────────
            // 1. Charger le snapshot
            // ─────────────────────────────
        	Connection conn = DbUtil.getConnection();
            JdbcCardSnapshotDao snapshotDao =
                new JdbcCardSnapshotDao(conn);

            CardSnapshot snapshot =
                snapshotDao.findById(snapshotId);

            if (snapshot == null) {
                return ResponseEntity.notFound().build();
            }
            String frameResource =
            	    "frames/"
            	    + snapshot.type.name()
            	    + "-"
            	    + snapshot.faction.name()
            	    + ".png";
            // ─────────────────────────────
            // 2. Cache key
            // ─────────────────────────────
            String cacheFileName =
                snapshot.visualSignature + "_" + PROFILE.name() + ".png";

            Path cachePath =
                CACHE_DIR.resolve(cacheFileName);

            // ─────────────────────────────
            // 3. CACHE HIT ✅
            // ─────────────────────────────
            if (Files.exists(cachePath)) {
                byte[] bytes = Files.readAllBytes(cachePath);

                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Cache-Control", "public, max-age=31536000, immutable")
                    .body(bytes);
            }


            // ─────────────────────────────
            // 4. Charger le render (illustration)
            // ─────────────────────────────
            JdbcCardRenderDao renderDao =
                new JdbcCardRenderDao(conn);

            CardRender render =
                renderDao.findByVisualSignature(
                    snapshot.visualSignature,
                    PROFILE
                );

            if (render == null) {
                return ResponseEntity.notFound().build();
            }

            // ─────────────────────────────
            // 5. Charger la frame (classpath)
            // ─────────────────────────────
            BufferedImage baseFrame = loadFrame(frameResource);

            // ─────────────────────────────
            // 6. Charger l’illustration (filesystem)
            // ─────────────────────────────
            BufferedImage illustration =
                loadIllustration(render.imagePath);

            // ─────────────────────────────
            // 7. Rendu FINAL carte
            // ─────────────────────────────
            BufferedImage card =
                RenderBackCard.renderIllustrationOnly(
                    baseFrame,
                    illustration
                );

            // ─────────────────────────────
            // 8. Écriture cache disque
            // ─────────────────────────────
            Files.createDirectories(CACHE_DIR);
            ImageIO.write(card, "PNG", cachePath.toFile());

            byte[] bytes = Files.readAllBytes(cachePath);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Cache-Control", "public, max-age=31536000, immutable")
                .body(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    

    private BufferedImage loadFrame(String frameResource) throws Exception {
        InputStream in =
            getClass()
                .getClassLoader()
                .getResourceAsStream(frameResource);

        if (in == null) {
            throw new IllegalStateException(
                "Frame introuvable dans le classpath : " + frameResource
            );
        }

        BufferedImage img = ImageIO.read(in);
        if (img == null) {
            throw new IllegalStateException(
                "Lecture frame impossible : " + frameResource
            );
        }

        return img;
    }

    private BufferedImage loadIllustration(String relativePath)
        throws Exception {

        Path illustrationPath =
            IMAGE_ROOT.resolve(relativePath).normalize();

        File file = illustrationPath.toFile();

        if (!file.exists()) {
            throw new IllegalStateException(
                "Illustration introuvable : " + file.getAbsolutePath()
            );
        }

        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IllegalStateException(
                "Lecture illustration impossible : " + file.getAbsolutePath()
            );
        }

        return img;
    }
}
