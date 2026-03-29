package dev.jos.back.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dev.jos.back.dto.payment.TicketResponseDTO;
import dev.jos.back.dto.payment.TransactionResponseDTO;
import dev.jos.back.util.enums.Phases;
import dev.jos.back.util.enums.SupportedLocale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class PdfTicketService {

    private static final Color OLYMPIC_BLUE = new Color(0x00, 0x3D, 0xA5);
    private static final Color OLYMPIC_GOLD = new Color(0xC8, 0xA8, 0x4B);
    private static final Color LIGHT_GRAY = new Color(0xF5, 0xF5, 0xF5);
    private static final Color MID_GRAY = new Color(0x6B, 0x6B, 0x6B);
    private static final Color BORDER_GRAY = new Color(0xE0, 0xE0, 0xE0);
    private static final Color DARK = new Color(0x1A, 0x1A, 0x1A);
    private static final Color DARK_MID = new Color(0x4A, 0x4A, 0x4A);

    private static final Font FONT_HEADER_TITLE = new Font(Font.HELVETICA, 20f, Font.BOLD, Color.WHITE);
    private static final Font FONT_HEADER_SUB = new Font(Font.HELVETICA, 9f, Font.NORMAL, OLYMPIC_GOLD);
    private static final Font FONT_BADGE = new Font(Font.HELVETICA, 8f, Font.BOLD, Color.WHITE);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 7f, Font.NORMAL, MID_GRAY);
    private static final Font FONT_EVENT_NAME = new Font(Font.HELVETICA, 18f, Font.BOLD, DARK);
    private static final Font FONT_EVENT_META = new Font(Font.HELVETICA, 10f, Font.NORMAL, MID_GRAY);
    private static final Font FONT_INFO_VALUE = new Font(Font.HELVETICA, 11f, Font.BOLD, DARK);
    private static final Font FONT_BARCODE = new Font(Font.COURIER, 8f, Font.NORMAL, MID_GRAY);
    private static final Font FONT_INSTRUCTIONS = new Font(Font.HELVETICA, 8f, Font.ITALIC, DARK_MID);
    private static final Font FONT_PRICE_LABEL = new Font(Font.HELVETICA, 11f, Font.NORMAL, MID_GRAY);
    private static final Font FONT_PRICE_VALUE = new Font(Font.HELVETICA, 18f, Font.BOLD, OLYMPIC_BLUE);
    private static final Font FONT_REF_LABEL = new Font(Font.HELVETICA, 8f, Font.NORMAL, MID_GRAY);
    private static final Font FONT_REF_VALUE = new Font(Font.HELVETICA, 8f, Font.BOLD, DARK);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 7f, Font.NORMAL, MID_GRAY);

    private static final EnumMap<SupportedLocale, PdfLabels> LABELS;

    static {
        LABELS = new EnumMap<>(SupportedLocale.class);

        LABELS.put(SupportedLocale.FR, new PdfLabels(
                "Jeux Olympiques · Billet officiel",
                "BILLET", "ÉPREUVE", "CATÉGORIE", "PLACES INCLUSES", "PHASE",
                "CODE-BARRE", "INSTRUCTIONS",
                "Présentez ce QR code à l'entrée du stade.\nValable pour la date indiquée uniquement.",
                "Référence de paiement", "Montant du billet",
                "© Paris 2024 · Comité d'Organisation des Jeux Olympiques",
                "Ce billet est personnel et non cessible.",
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH),
                Locale.FRENCH,
                phaseMapFr()
        ));

        LABELS.put(SupportedLocale.EN, new PdfLabels(
                "Olympic Games · Official Ticket",
                "TICKET", "EVENT", "CATEGORY", "INCLUDED SEATS", "PHASE",
                "BARCODE", "INSTRUCTIONS",
                "Present this QR code at the stadium entrance.\nValid for the indicated date only.",
                "Payment reference", "Ticket price",
                "© Paris 2024 · Paris 2024 Organising Committee",
                "This ticket is personal and non-transferable.",
                DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy 'at' HH:mm", Locale.ENGLISH),
                Locale.ENGLISH,
                phaseMapEn()
        ));

        LABELS.put(SupportedLocale.DE, new PdfLabels(
                "Olympische Spiele · Offizielles Ticket",
                "TICKET", "WETTKAMPF", "KATEGORIE", "ENTHALTENE PLÄTZE", "PHASE",
                "BARCODE", "HINWEISE",
                "Zeigen Sie diesen QR-Code am Stadioneingang vor.\nNur für das angegebene Datum gültig.",
                "Zahlungsreferenz", "Ticketpreis",
                "© Paris 2024 · Organisationskomitee Paris 2024",
                "Dieses Ticket ist persönlich und nicht übertragbar.",
                DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy 'um' HH:mm", Locale.GERMAN),
                Locale.GERMAN,
                phaseMapDe()
        ));

        LABELS.put(SupportedLocale.ES, new PdfLabels(
                "Juegos Olímpicos · Entrada oficial",
                "ENTRADA", "EVENTO", "CATEGORÍA", "PLAZAS INCLUIDAS", "FASE",
                "CÓDIGO DE BARRAS", "INSTRUCCIONES",
                "Presente este código QR en la entrada del estadio.\nVálido únicamente para la fecha indicada.",
                "Referencia de pago", "Precio de la entrada",
                "© París 2024 · Comité Organizador de París 2024",
                "Esta entrada es personal e intransferible.",
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.of("es")),
                Locale.of("es"),
                phaseMapEs()
        ));
    }

    private static Map<Phases, String> phaseMapFr() {
        return Map.ofEntries(
                Map.entry(Phases.SERIES, "Séries"),
                Map.entry(Phases.REPECHAGE, "Repêchage"),
                Map.entry(Phases.QUALIFICATION, "Qualification"),
                Map.entry(Phases.TOUR_PRELIMINAIRE, "Tour préliminaire"),
                Map.entry(Phases.PHASE_DE_POULES, "Phase de poules"),
                Map.entry(Phases.PHASE_DE_GROUPES, "Phase de groupes"),
                Map.entry(Phases.SOIXANTE_QUATRIEME_DE_FINALE, "64e de finale"),
                Map.entry(Phases.TRENTE_DEUXIEME_DE_FINALE, "32e de finale"),
                Map.entry(Phases.SEIZIEME_DE_FINALE, "16e de finale"),
                Map.entry(Phases.QUART_DE_FINALE, "Quart de finale"),
                Map.entry(Phases.DEMI_FINALE, "Demi-finale"),
                Map.entry(Phases.FINALE, "Finale"),
                Map.entry(Phases.CONTRE_LA_MONTRE, "Contre-la-montre"),
                Map.entry(Phases.CLASSEMENT, "Classement"),
                Map.entry(Phases.RELAIS_MIXTE, "Relais mixte"),
                Map.entry(Phases.EPREUVE_PAR_EQUIPES, "Épreuve par équipes"),
                Map.entry(Phases.MATCH_BRONZE, "Match pour la médaille de bronze")
        );
    }

    private static Map<Phases, String> phaseMapEn() {
        return Map.ofEntries(
                Map.entry(Phases.SERIES, "Heats"),
                Map.entry(Phases.REPECHAGE, "Repechage"),
                Map.entry(Phases.QUALIFICATION, "Qualification"),
                Map.entry(Phases.TOUR_PRELIMINAIRE, "Preliminary Round"),
                Map.entry(Phases.PHASE_DE_POULES, "Pool Stage"),
                Map.entry(Phases.PHASE_DE_GROUPES, "Group Stage"),
                Map.entry(Phases.SOIXANTE_QUATRIEME_DE_FINALE, "Round of 128"),
                Map.entry(Phases.TRENTE_DEUXIEME_DE_FINALE, "Round of 64"),
                Map.entry(Phases.SEIZIEME_DE_FINALE, "Round of 32"),
                Map.entry(Phases.QUART_DE_FINALE, "Quarterfinal"),
                Map.entry(Phases.DEMI_FINALE, "Semifinal"),
                Map.entry(Phases.FINALE, "Final"),
                Map.entry(Phases.CONTRE_LA_MONTRE, "Time Trial"),
                Map.entry(Phases.CLASSEMENT, "Ranking"),
                Map.entry(Phases.RELAIS_MIXTE, "Mixed Relay"),
                Map.entry(Phases.EPREUVE_PAR_EQUIPES, "Team Event"),
                Map.entry(Phases.MATCH_BRONZE, "Bronze Medal Match")
        );
    }

    private static Map<Phases, String> phaseMapDe() {
        return Map.ofEntries(
                Map.entry(Phases.SERIES, "Vorläufe"),
                Map.entry(Phases.REPECHAGE, "Hoffnungslauf"),
                Map.entry(Phases.QUALIFICATION, "Qualifikation"),
                Map.entry(Phases.TOUR_PRELIMINAIRE, "Vorrunde"),
                Map.entry(Phases.PHASE_DE_POULES, "Poolphase"),
                Map.entry(Phases.PHASE_DE_GROUPES, "Gruppenphase"),
                Map.entry(Phases.SOIXANTE_QUATRIEME_DE_FINALE, "Runde der letzten 128"),
                Map.entry(Phases.TRENTE_DEUXIEME_DE_FINALE, "Runde der letzten 64"),
                Map.entry(Phases.SEIZIEME_DE_FINALE, "Runde der letzten 32"),
                Map.entry(Phases.QUART_DE_FINALE, "Viertelfinale"),
                Map.entry(Phases.DEMI_FINALE, "Halbfinale"),
                Map.entry(Phases.FINALE, "Finale"),
                Map.entry(Phases.CONTRE_LA_MONTRE, "Einzelzeitfahren"),
                Map.entry(Phases.CLASSEMENT, "Platzierungsrunde"),
                Map.entry(Phases.RELAIS_MIXTE, "Gemischte Staffel"),
                Map.entry(Phases.EPREUVE_PAR_EQUIPES, "Mannschaftswettbewerb"),
                Map.entry(Phases.MATCH_BRONZE, "Spiel um Bronze")
        );
    }

    private static Map<Phases, String> phaseMapEs() {
        return Map.ofEntries(
                Map.entry(Phases.SERIES, "Series"),
                Map.entry(Phases.REPECHAGE, "Repesca"),
                Map.entry(Phases.QUALIFICATION, "Clasificación"),
                Map.entry(Phases.TOUR_PRELIMINAIRE, "Ronda preliminar"),
                Map.entry(Phases.PHASE_DE_POULES, "Fase de grupos"),
                Map.entry(Phases.PHASE_DE_GROUPES, "Fase de grupos"),
                Map.entry(Phases.SOIXANTE_QUATRIEME_DE_FINALE, "Ronda de 128"),
                Map.entry(Phases.TRENTE_DEUXIEME_DE_FINALE, "Ronda de 64"),
                Map.entry(Phases.SEIZIEME_DE_FINALE, "Ronda de 32"),
                Map.entry(Phases.QUART_DE_FINALE, "Cuartos de final"),
                Map.entry(Phases.DEMI_FINALE, "Semifinal"),
                Map.entry(Phases.FINALE, "Final"),
                Map.entry(Phases.CONTRE_LA_MONTRE, "Contrarreloj"),
                Map.entry(Phases.CLASSEMENT, "Clasificación"),
                Map.entry(Phases.RELAIS_MIXTE, "Relevo mixto"),
                Map.entry(Phases.EPREUVE_PAR_EQUIPES, "Prueba por equipos"),
                Map.entry(Phases.MATCH_BRONZE, "Partido por el bronce")
        );
    }

    private PdfLabels getLabels(String locale) {
        return LABELS.get(SupportedLocale.from(locale));
    }

    static PdfLabels labelsFor(String locale) {
        return LABELS.get(SupportedLocale.from(locale));
    }

    public byte[] generate(TransactionResponseDTO dto) {
        return generate(dto, "fr");
    }


    public byte[] generate(TransactionResponseDTO dto, String locale) {
        PdfLabels labels = getLabels(locale);
        List<TicketResponseDTO> tickets = dto.tickets();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, baos);
            document.open();

            for (int i = 0; i < tickets.size(); i++) {
                if (i > 0) document.newPage();
                addTicketPage(document, tickets.get(i), dto, i, tickets.size(), labels);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Échec génération PDF billets pour transaction {}: {}", dto.id(), e.getMessage(), e);
            throw new RuntimeException("Impossible de générer le PDF des billets", e);
        }
    }

    private void addTicketPage(Document doc, TicketResponseDTO ticket,
                               TransactionResponseDTO tx, int index, int total,
                               PdfLabels labels) throws Exception {
        float margin = 32f;
        float pageW = doc.getPageSize().getWidth();

        PdfPTable header = new PdfPTable(2);
        header.setTotalWidth(pageW);
        header.setLockedWidth(true);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(OLYMPIC_BLUE);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(0);
        titleCell.setPaddingLeft(margin);
        titleCell.setPaddingTop(20);
        titleCell.setPaddingBottom(20);

        Paragraph titlePara = new Paragraph("PARIS 2024", FONT_HEADER_TITLE);
        titlePara.setSpacingAfter(3);
        Paragraph subtitlePara = new Paragraph(labels.subtitle(), FONT_HEADER_SUB);
        titleCell.addElement(titlePara);
        titleCell.addElement(subtitlePara);

        PdfPCell badgeCell = new PdfPCell();
        badgeCell.setBackgroundColor(OLYMPIC_BLUE);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        badgeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        badgeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        badgeCell.setPaddingRight(margin);

        PdfPTable badgeInner = new PdfPTable(1);
        badgeInner.setWidthPercentage(60);
        PdfPCell badge = new PdfPCell(
                new Phrase(labels.ticketLabel() + " " + (index + 1) + "/" + total, FONT_BADGE));
        badge.setBackgroundColor(OLYMPIC_GOLD);
        badge.setBorder(Rectangle.NO_BORDER);
        badge.setHorizontalAlignment(Element.ALIGN_CENTER);
        badge.setPadding(5);
        badgeInner.addCell(badge);
        badgeCell.addElement(badgeInner);

        header.addCell(titleCell);
        header.addCell(badgeCell);
        doc.add(header);

        PdfPTable stripe = new PdfPTable(1);
        stripe.setTotalWidth(pageW);
        stripe.setLockedWidth(true);
        PdfPCell sc = new PdfPCell(new Phrase(" "));
        sc.setBackgroundColor(OLYMPIC_GOLD);
        sc.setBorder(Rectangle.NO_BORDER);
        sc.setFixedHeight(3f);
        stripe.addCell(sc);
        doc.add(stripe);

        PdfPTable body = new PdfPTable(1);
        body.setTotalWidth(pageW - margin * 2);
        body.setLockedWidth(true);
        body.setSpacingBefore(20f);

        addBodySection(body, buildEventSection(ticket, labels), margin);
        addSeparatorRow(body);
        addBodySection(body, buildInfoGrid(ticket, labels), margin);
        addSeparatorRow(body);
        addBodySection(body, buildQrSection(ticket, labels), margin);
        addSeparatorRow(body);
        addBodySection(body, buildPriceRow(ticket, tx, labels), margin);

        doc.add(body);

        addFooter(doc, pageW, margin, labels);
    }


    private PdfPTable buildEventSection(TicketResponseDTO ticket, PdfLabels labels) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        addLabelRow(t, labels.eventLabel());
        addValueRow(t, ticket.event().name(), FONT_EVENT_NAME);
        addValueRow(t, ticket.event().eventDate().format(labels.dateFmt()), FONT_EVENT_META);
        addValueRow(t, ticket.event().location() + " · " + ticket.event().city(), FONT_EVENT_META);
        return t;
    }


    private PdfPTable buildInfoGrid(TicketResponseDTO ticket, PdfLabels labels) throws DocumentException {
        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 1f, 1f});
        addInfoBox(t, labels.categoryLabel(), ticket.offer().name());
        addInfoBox(t, labels.seatsLabel(), String.valueOf(ticket.offer().numberOfTickets()));
        addInfoBox(t, labels.phaseLabel(), formatPhase(ticket.event().phase(), labels));
        return t;
    }

    private PdfPTable buildQrSection(TicketResponseDTO ticket, PdfLabels labels) throws Exception {
        byte[] qrBytes = generateQrCode(ticket.barcode());
        Image qrImage = Image.getInstance(qrBytes);
        qrImage.scaleAbsolute(100f, 100f);

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 2.5f});

        PdfPCell qrCell = new PdfPCell(qrImage, false);
        qrCell.setBackgroundColor(LIGHT_GRAY);
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setPadding(10f);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(LIGHT_GRAY);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(12f);
        infoCell.setPaddingLeft(8f);

        Paragraph bcLabel = new Paragraph(labels.barcodeLabel(), FONT_LABEL);
        bcLabel.setSpacingAfter(2f);
        Paragraph bcValue = new Paragraph(ticket.barcode(), FONT_BARCODE);
        bcValue.setSpacingAfter(10f);

        Paragraph instrLabel = new Paragraph(labels.instructionsLabel(), FONT_LABEL);
        instrLabel.setSpacingAfter(2f);
        Paragraph instrValue = new Paragraph(labels.instructions(), FONT_INSTRUCTIONS);

        infoCell.addElement(bcLabel);
        infoCell.addElement(bcValue);
        infoCell.addElement(instrLabel);
        infoCell.addElement(instrValue);

        t.addCell(qrCell);
        t.addCell(infoCell);
        return t;
    }

    private PdfPTable buildPriceRow(TicketResponseDTO ticket, TransactionResponseDTO tx,
                                    PdfLabels labels) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.5f, 1f});

        PdfPCell refCell = new PdfPCell();
        refCell.setBorder(Rectangle.NO_BORDER);
        refCell.setPaddingBottom(4f);
        Paragraph refLabel = new Paragraph(labels.paymentRefLabel(), FONT_REF_LABEL);
        refLabel.setSpacingAfter(2f);
        Paragraph refValue = new Paragraph(tx.paymentReference(), FONT_REF_VALUE);
        refCell.addElement(refLabel);
        refCell.addElement(refValue);

        PdfPCell priceCell = new PdfPCell();
        priceCell.setBorder(Rectangle.NO_BORDER);
        priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph priceLabel = new Paragraph(labels.ticketPriceLabel(), FONT_PRICE_LABEL);
        priceLabel.setAlignment(Element.ALIGN_RIGHT);
        priceLabel.setSpacingAfter(2f);
        String priceStr = String.format(labels.priceLocale(), "%.2f\u00a0€", ticket.price());
        Paragraph priceValue = new Paragraph(priceStr, FONT_PRICE_VALUE);
        priceValue.setAlignment(Element.ALIGN_RIGHT);
        priceCell.addElement(priceLabel);
        priceCell.addElement(priceValue);

        t.addCell(refCell);
        t.addCell(priceCell);
        return t;
    }

    private String formatPhase(Phases phase, PdfLabels labels) {
        if (phase == null) return "—";
        return labels.phaseNames().getOrDefault(phase,
                phase.name().replace('_', ' ').toLowerCase(Locale.ROOT));
    }


    private void addBodySection(PdfPTable body, PdfPTable section, float margin) {
        PdfPCell cell = new PdfPCell(section);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        cell.setPaddingLeft(margin);
        cell.setPaddingRight(margin);
        cell.setPaddingTop(16f);
        cell.setPaddingBottom(16f);
        body.addCell(cell);
    }

    private void addSeparatorRow(PdfPTable body) {
        PdfPCell sep = new PdfPCell(new Phrase(" "));
        sep.setBackgroundColor(BORDER_GRAY);
        sep.setBorder(Rectangle.NO_BORDER);
        sep.setFixedHeight(1f);
        body.addCell(sep);
    }

    private void addLabelRow(PdfPTable t, String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, FONT_LABEL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(3f);
        t.addCell(cell);
    }

    private void addValueRow(PdfPTable t, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(3f);
        t.addCell(cell);
    }

    private void addInfoBox(PdfPTable t, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderColor(Color.WHITE);
        cell.setBorderWidth(3f);
        cell.setPadding(10f);
        Paragraph lbl = new Paragraph(label, FONT_LABEL);
        lbl.setSpacingAfter(4f);
        Paragraph val = new Paragraph(value, FONT_INFO_VALUE);
        cell.addElement(lbl);
        cell.addElement(val);
        t.addCell(cell);
    }

    private void addFooter(Document doc, float pageW, float margin, PdfLabels labels)
            throws DocumentException {
        PdfPTable footer = new PdfPTable(2);
        footer.setTotalWidth(pageW);
        footer.setLockedWidth(true);
        footer.setSpacingBefore(16f);

        PdfPCell left = new PdfPCell(new Phrase(labels.copyright(), FONT_FOOTER));
        left.setBackgroundColor(LIGHT_GRAY);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingLeft(margin);
        left.setPaddingTop(10f);
        left.setPaddingBottom(10f);

        PdfPCell right = new PdfPCell(new Phrase(labels.nonTransferable(), FONT_FOOTER));
        right.setBackgroundColor(LIGHT_GRAY);
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setPaddingRight(margin);
        right.setPaddingTop(10f);
        right.setPaddingBottom(10f);

        footer.addCell(left);
        footer.addCell(right);
        doc.add(footer);
    }

    private byte[] generateQrCode(String content) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == Color.BLACK.getRGB()) {
                    image.setRGB(x, y, OLYMPIC_BLUE.getRGB());
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }

    record PdfLabels(
            String subtitle,
            String ticketLabel,
            String eventLabel,
            String categoryLabel,
            String seatsLabel,
            String phaseLabel,
            String barcodeLabel,
            String instructionsLabel,
            String instructions,
            String paymentRefLabel,
            String ticketPriceLabel,
            String copyright,
            String nonTransferable,
            DateTimeFormatter dateFmt,
            Locale priceLocale,
            Map<Phases, String> phaseNames
    ) {
    }
}
