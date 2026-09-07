package io.point3.p3api.inquiry.application.realtime;

import java.util.UUID;

public record InquiryListReaderChangedEvent(UUID inquiryId, UUID readerUserId) {}
