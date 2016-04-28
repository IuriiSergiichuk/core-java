/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spine3.examples.aggregate;

import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.base.UserId;
import org.spine3.examples.aggregate.command.AddOrderLine;
import org.spine3.examples.aggregate.command.CreateOrder;
import org.spine3.examples.aggregate.command.PayForOrder;
import org.spine3.protobuf.Messages;

import static org.spine3.time.ZoneOffsets.UTC;

/**
 * Utility class for generating sample command requests.
 *
 * @author Mikhail Melnik
 */
/*package*/ class Requests {

    private static final String ORDER_BC_NAME = "Order BC";

    public static Command createOrder(UserId userId, OrderId orderId) {
        final CreateOrder msg = CreateOrder.newBuilder()
                .setOrderId(orderId)
                .build();
        final CommandContext context = Commands.createContext(userId, UTC, ORDER_BC_NAME);
        final Command cmd = Commands.create(msg, context);
        return cmd;
    }

    public static Command addOrderLine(UserId userId, OrderId orderId) {
        final double price = 51.33;
        final Book book = Book.newBuilder()
                .setBookId(BookId.newBuilder().setISBN("978-0321125217").build())
                .setAuthor("Eric Evans")
                .setTitle("Domain Driven Design.")
                .setPrice(price)
                .build();
        final int quantity = 1;
        final double totalPrice = book.getPrice() * quantity;
        final OrderLine orderLine = OrderLine.newBuilder()
                .setProductId(Messages.toAny(book.getBookId()))
                .setQuantity(quantity)
                .setTotal(totalPrice)
                .build();
        final AddOrderLine msg = AddOrderLine.newBuilder()
                .setOrderId(orderId)
                .setOrderLine(orderLine).build();
        final CommandContext context = Commands.createContext(userId, UTC, ORDER_BC_NAME);
        final Command cmd = Commands.create(msg, context);
        return cmd;
    }

    public static Command payForOrder(UserId userId, OrderId orderId) {
        final BillingInfo billingInfo = BillingInfo.newBuilder().setInfo("Payment info is here.").build();
        final PayForOrder msg = PayForOrder.newBuilder()
                .setOrderId(orderId)
                .setBillingInfo(billingInfo)
                .build();
        final CommandContext context = Commands.createContext(userId, UTC, ORDER_BC_NAME);
        final Command cmd = Commands.create(msg, context);
        return cmd;
    }

    private Requests() {}
}
