import { useQuery } from "@tanstack/react-query";
import { getRecentOrders } from "@/api/orders";
import { Loader2 } from "lucide-react";

export default function OrdersPage() {
  const { data: orders, isLoading, isError } = useQuery({
    queryKey: ["orders"],
    queryFn: getRecentOrders,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (isError) {
    return (
      <p className="text-sm text-destructive py-8 text-center">
        Failed to load orders.
      </p>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <div className="py-16 text-center text-muted-foreground text-sm">
        No orders yet. Head to the Shop page to add meals to your cart.
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-4">
      <h1 className="text-2xl font-semibold">Recent Orders</h1>
      <div className="space-y-3">
        {orders.map((order) => (
          <div key={order.id} className="border rounded-lg p-4 space-y-1">
            <p className="text-sm text-muted-foreground">
              {new Date(order.createdAt).toLocaleString()}
            </p>
            <p className="text-sm font-medium">
              {order.mealNames.length > 0
                ? order.mealNames.join(", ")
                : <span className="text-muted-foreground italic">No meals recorded</span>
              }
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
