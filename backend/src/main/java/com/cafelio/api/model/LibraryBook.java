package com.cafelio.api.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "library_books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_library_book_user_open_library",
                        columnNames = {"user_id", "open_library_id"}
                )
        }
)
public class LibraryBook {

    public LibraryBook(){

    }

    public LibraryBook(User user, String openLibraryId, String title, Integer firstPublishYear, String coverUrl, ReadingStatus status, List<String> authors, Set<BookTag> tags, Integer pages){
        this.user = user;
        this.openLibraryId = openLibraryId;
        this.title = title;
        this.firstPublishYear = firstPublishYear;
        this.coverUrl = coverUrl;
        this.status = status;
        this.authors = authors;
        this.tags = tags;
        this.pages = pages;
    }

    public UUID getId() {
        return id;
    }

    public String getOpenLibraryId(){
        return openLibraryId;
    }

    public String getTitle(){
        return title;
    }

    public Integer getFirstPublishYear(){
        return firstPublishYear;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public Set<BookTag> getTags() {
        return tags;
    }

    public void setStatus(ReadingStatus status){
        this.status = status;
    }

    public void setTags(Set<BookTag> tags) {
        this.tags = tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAuthorState() {
        return authorState;
    }

    public void setAuthorState(String authorState) {
        this.authorState = authorState;
    }

    public LiteraryGenre getLiteraryGenre() {
        return literaryGenre;
    }

    public void setLiteraryGenre(LiteraryGenre literaryGenre) {
        this.literaryGenre = literaryGenre;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "open_library_id", nullable = false)
    private String openLibraryId;

    @Column(nullable = false)
    private String title;

    @Column(name = "first_publish_year")
    private Integer firstPublishYear;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "cover_url")
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "author_state", length = 2)
    private String authorState;

    @Enumerated(EnumType.STRING)
    @Column(name = "literary_genre", length = 40)
    private LiteraryGenre literaryGenre;

    @ElementCollection
    @CollectionTable(
            name = "library_book_authors",
            joinColumns = @JoinColumn(name = "library_book_id")
    )
    @Column(name = "author", nullable = false)
    private List<String> authors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "library_book_tags",
            joinColumns = @JoinColumn(name = "library_book_id")
    )
    @Column(name = "tag", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BookTag> tags = new HashSet<>();
}