package app.openair.model.logic

import app.openair.types.StatsPeriod
import java.time.Period
import java.util.*

class DateUtil {
    companion object{
        fun getDateAt(startDate: Date, statsPeriod: StatsPeriod): Date{
            val period = when(statsPeriod){
                StatsPeriod.DAY -> Period.ofDays(-1)
                StatsPeriod.WEEK -> Period.ofWeeks(-1)
                StatsPeriod.MONTH -> Period.ofMonths(-1)
                StatsPeriod.YEAR -> Period.ofYears(-1)
            }
            return calculateDateFromPeriod(startDate, period)
        }

        private fun calculateDateFromPeriod(startDate: Date, period: Period): Date {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.DAY_OF_YEAR, period.days)
            calendar.add(Calendar.MONTH, period.months)
            calendar.add(Calendar.YEAR, period.years)

            return calendar.time
        }
    }
}