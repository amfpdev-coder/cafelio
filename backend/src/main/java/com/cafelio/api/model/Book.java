package com.cafelio.api.model;

import jakarta.persistence.*;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_books_isbn13",
                        columnNames = {"isbn13"}
                )
        }
)
public class Book {

    public Book() {

    }

    public Book(String title, BookSource source) {
        setTitle(title);
        this.source = source;
    }

    // Sem acento, minusculo, sem pontuacao e sem espaco repetido.
    // E a segunda rede contra duplicata: quem cadastra sem ISBN cai aqui.
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public UUID getId() {
        return id;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getWorkKey() {
        return workKey;
    }

    public void setWorkKey(String workKey) {
        this.workKey = workKey;
    }

    public String getTitle() {
        return title;
    }

    // O titulo normalizado nunca e preenchido por fora: ele acompanha o titulo
    public void setTitle(String title) {
        this.title = title;
        this.titleNormalized = normalizar(title);
    }

    public String getTitleNormalized() {
        return titleNormalized;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }

    public Integer getFirstPublishYear() {
        return firstPublishYear;
    }

    public void setFirstPublishYear(Integer firstPublishYear) {
        this.firstPublishYear = firstPublishYear;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LiteraryGenre getLiteraryGenre() {
        return literaryGenre;
    }

    public void setLiteraryGenre(LiteraryGenre literaryGenre) {
        this.literaryGenre = literaryGenre;
    }

    public BookSource getSource() {
        return source;
    }

    public void setSource(BookSource source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "isbn13", length = 13)
    private String isbn13;

    @Column(name = "isbn10", length = 10)
    private String isbn10;

    @Column(name = "work_key", length = 40)
    private String workKey;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "title_normalized", nullable = false, length = 300)
    private String titleNormalized;

    @Column(name = "subtitle", length = 300)
    private String subtitle;

    @Column(name = "publisher", length = 160)
    private String publisher;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(name = "first_publish_year")
    private Integer firstPublishYear;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "language", length = 8)
    private String language;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "country", length = 2)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "literary_genre", length = 40)
    private LiteraryGenre literaryGenre;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private BookSource source;

    @Column(name = "external_id", length = 80)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.APPROVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @ElementCollection
    @CollectionTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id")
    )
    @Column(name = "author", nullable = false)
    private List<String> authors = new ArrayList<>();
}
