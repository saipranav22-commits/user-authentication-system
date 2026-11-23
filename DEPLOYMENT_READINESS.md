# Deployment Readiness Assessment

## Platform: Render Free Web Service
- **Plan**: Free ($0/month).
- **Payment Requirement**: No credit card or debit card required.
- **Resource Limits**: 512 MB RAM, 0.1 CPU core, 750 free instance hours shared monthly.
- **Memory Optimization**: Configured `-XX:+UseSerialGC -Xmx384m -Xms128m -Xss512k` to prevent out-of-memory termination.
- **Spin-down Behavior**: Service enters sleep mode after 15 minutes of inactivity and wakes automatically upon receiving incoming HTTP traffic.

## Database: Neon PostgreSQL Free Tier
- **Plan**: Serverless PostgreSQL Free tier.
- **Payment Requirement**: No credit card required.
- **Features**: Dedicated storage with SSL required connection strings.
- **Isolation**: Clean, dedicated project without interference with any other services.
