package com.choikang.back.config;

import com.choikang.back.entity.Member;
import com.choikang.back.entity.MemberRank;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.LocalDateTime;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RankBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job updateRankingJob() {
        return new JobBuilder("updateRankingJob", jobRepository)
                .start(updateRankingStep())
                .build();
    }

    @Bean
    public Step updateRankingStep() {
        return new StepBuilder("updateRankingStep", jobRepository)
                .<Member, MemberRank>chunk(100, transactionManager)
                .reader(memberReader())
                .processor(rankProcessor())
                .writer(rankWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Member> memberReader() {
        return new JpaPagingItemReaderBuilder<Member>()
                .name("memberReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT m FROM Member m ORDER BY m.score DESC")
                .build();
    }

    @Bean
    public ItemProcessor<Member, MemberRank> rankProcessor() {
        return new ItemProcessor<Member, MemberRank>() {
            private int rank = 0;

            @Override
            public MemberRank process(Member member) {
                rank++;
                return MemberRank.builder()
                        .memberId(member.getMemberId())
                        .score(member.getScore())
                        .rank(rank)
                        .updatedAt(LocalDateTime.now())
                        .build();
            }
        };
    }

    @Bean
    public JpaItemWriter<MemberRank> rankWriter() {
        return new JpaItemWriterBuilder<MemberRank>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}