package com.lucasnorgaard.tstudioservice.models.marketplace;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

import lombok.Data;

@AllArgsConstructor
@Data
public class Publisher {

        public String publisherId;
        public String publisherName;
        public String displayName;
        public String flags;
        public @Nullable String domain;
        public boolean isDomainVerified;
}
