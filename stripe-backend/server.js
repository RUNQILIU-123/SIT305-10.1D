const express = require('express');
const app = express();
const cors = require('cors');
require('dotenv').config();

// Initialize Stripe with the Secret Key from .env file
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);

app.use(cors());
app.use(express.json());

// Basic health check endpoint
app.get('/', (req, res) => {
    res.send('Stripe Backend for LLM Learning Assistant is running!');
});

/**
 * Endpoint to create a PaymentIntent.
 * This is called by the Android app before launching the PaymentSheet.
 */
app.post('/create-payment-intent', async (req, res) => {
    const { amount, planName } = req.body;

    // Basic validation
    if (!amount || !planName) {
        return res.status(400).send({
            error: "Missing amount or planName in request body.",
        });
    }

    try {
        // Create a PaymentIntent with the order amount and currency
        const paymentIntent = await stripe.paymentIntents.create({
            amount: amount, // in cents (e.g., 999 for $9.99)
            currency: 'usd',
            // In a real app, you might want to enable specific payment methods
            automatic_payment_methods: {
                enabled: true,
            },
            metadata: {
                planName: planName
            }
        });

        // Send the clientSecret to the Android app
        res.send({
            clientSecret: paymentIntent.client_secret,
        });
    } catch (e) {
        console.error("Stripe Error:", e.message);
        res.status(400).send({
            error: e.message,
        });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
    console.log(`For Android Emulator, use http://10.0.2.2:${PORT} as the base URL`);
});
