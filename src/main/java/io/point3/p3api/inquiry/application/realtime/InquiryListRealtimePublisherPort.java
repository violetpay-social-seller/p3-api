package io.point3.p3api.inquiry.application.realtime;

public interface InquiryListRealtimePublisherPort {

  void publish(InquiryListRealtimeEvent event);
}
