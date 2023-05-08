package com.example.festo.order.adapter.out.persistence;

import com.example.festo.booth.adapter.out.persistence.BoothEntity;
import com.example.festo.booth.adapter.out.persistence.BoothRepository;
import com.example.festo.member.adapter.out.persistence.MemberRepository;
import com.example.festo.member.domain.Member;
import com.example.festo.order.adapter.in.web.model.OrderSummary;
import com.example.festo.order.application.port.out.LoadOrderPort;
import com.example.festo.order.application.port.out.PlaceOrderPort;
import com.example.festo.order.application.port.out.UpdateOrderStatusPort;
import com.example.festo.order.domain.BoothInfo;
import com.example.festo.order.domain.Order;
import com.example.festo.order.domain.Orderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements PlaceOrderPort, LoadOrderPort, UpdateOrderStatusPort {

    private final OrderRepository orderRepository;

    private final MemberRepository memberRepository;

    private final BoothRepository boothRepository;

    @Override
    public void placeOrder(Order order) {
        Member orderer = memberRepository.findById(order.getOrderer()
                                                        .getMemberId())
                                         .orElseThrow(NoSuchElementException::new);
        BoothEntity boothEntity = boothRepository.findById(order.getBoothInfo()
                                                                .getBoothId())
                                                 .orElseThrow(NoSuchElementException::new);

        OrderEntity orderEntity = OrderEntity.builder()
                                             .orderer(orderer)
                                             .booth(boothEntity)
                                             .orderLines(order.getOrderLines())
                                             .totalAmounts(order.getTotalAmounts())
                                             .orderStatus(order.getOrderStatus())
                                             .build();

        orderRepository.save(orderEntity);
    }

    @Override
    public int nextOrderNo(Long boothId) {
        return orderRepository.nextOrderNo(boothId);
    }

    @Override
    public Order loadOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                                                 .orElseThrow(NoSuchElementException::new);

        return mapToOrderDomain(orderEntity);
    }

    @Override
    public List<Order> loadOrdersByOrdererId(Long ordererId) {

        return orderRepository.findOrdersByOrdererId(ordererId)
                              .stream()
                              .map(this::mapToOrderDomain)
                              .collect(Collectors.toList());
    }

    @Override
    public Order updateOrderStatus(Order order) {
        OrderEntity orderEntity = orderRepository.findById(order.getOrderId())
                                                 .orElseThrow(NoSuchElementException::new);

        orderEntity.updateStatus(order.getOrderStatus());
        orderRepository.save(orderEntity);

        return order;
    }

    private Order mapToOrderDomain(OrderEntity orderEntity) {
        Member member = orderEntity.getOrderer();
        BoothEntity booth = orderEntity.getBooth();

        Orderer orderer = new Orderer(member.getId(), member.getNickname());
        BoothInfo boothInfo = new BoothInfo(booth.getBoothId(), booth.getOwner().getId(), booth.getName());

        return Order.builder()
                    .orderNo(orderEntity.getOrderNo())
                    .boothInfo(boothInfo)
                    .orderer(orderer)
                    .orderTime(orderEntity.getOrderTime())
                    .orderLines(orderEntity.getOrderLines())
                    .build();
    }
}
